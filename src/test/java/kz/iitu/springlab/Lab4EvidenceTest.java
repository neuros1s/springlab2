package kz.iitu.springlab;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
@SpringBootTest(properties={"logging.pattern.console=%msg%n", "spring.profiles.active="})
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class Lab4EvidenceTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    private final Path folder = Path.of("evidence");
    private final Map<String,Object> results = new LinkedHashMap<>();
    private String call(String name, MockHttpServletRequestBuilder request, int expected,
                        CapturedOutput output) throws Exception {
        int start = output.getAll().length();
        var response = mvc.perform(request).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(expected);
        String logs = output.getAll().substring(start).lines()
            .filter(s -> s.matches(".*\\[(TRACE|AUDIT|LOG|TIME)\\].*"))
            .collect(Collectors.joining("\n"));
        Files.writeString(folder.resolve(name+".log"), logs+"\n");
        Files.writeString(folder.resolve(name+".response.txt"), response.getContentAsString());
        results.put(name, Map.of("status",response.getStatus(),"body",response.getContentAsString()));
        return logs;
    }
    private long count(String logs, String marker) {
        return logs.lines().filter(line -> line.contains(marker)).count();
    }
    @Test void captureAndVerifyRequiredExperiments(CapturedOutput output) throws Exception {
        Files.createDirectories(folder);
        String success = call("success",get("/api/lab4/items?limit=5"),200,output);
        String[] order = {"[AUDIT] start CATALOG_LIST", "[LOG] ->", "[TIME] SLOW:",
                          "[LOG] <-", "[AUDIT] CATALOG_LIST success"};
        int previous=-1;
        for(String token:order) { int at=success.indexOf(token); assertThat(at).isGreaterThan(previous); previous=at; }
        assertThat(success).contains("args=[5]", "Item no. 5");
        String failed=call("failure",delete("/api/lab4/item/0"),400,output);
        assertThat(failed).contains("[LOG] !!", "IllegalArgumentException", "Invalid identifier: 0", "CATALOG_REMOVE failure");
        assertThat(failed).doesNotContain("[LOG] <-", "CATALOG_REMOVE success");
        assertThat(count(failed,"[TRACE]")).isEqualTo(2);
        String noAudit=call("no-audit",get("/api/lab4/item/5"),200,output);
        assertThat(noAudit).doesNotContain("[AUDIT]").contains("depth=0");
        call("proxy",get("/api/lab4/proxy"),200,output);
        var proxy=mapper.readTree(Files.readString(folder.resolve("proxy.response.txt")));
        assertThat(proxy.get("isAopProxy").asText()).isEqualTo("true");
        assertThat(proxy.get("isCglib").asText()).isEqualTo("true");
        assertThat(proxy.get("className").asText()).contains("$$SpringCGLIB$$");
        String external=call("external",delete("/api/lab4/item/5"),200,output);
        assertThat(count(external,"[AUDIT]")).isEqualTo(2);
        String before=call("self-before",get("/api/lab4/remove-twice/5"),200,output);
        assertThat(count(before,"[AUDIT]")).isZero();
        assertThat(count(before,"[LOG]")).isEqualTo(2);
        assertThat(before).doesNotContain("depth=1");
        String after=call("self-after",get("/api/lab4/remove-twice-fixed/5"),200,output);
        assertThat(count(after,"[AUDIT]")).isEqualTo(4);
        assertThat(count(after,"[LOG]")).isEqualTo(6);
        assertThat(count(after,"depth=1")).isEqualTo(4);
        String nestedFailure=call("nested-failure",get("/api/lab4/remove-twice-fixed/0"),400,output);
        assertThat(count(nestedFailure,"[TRACE]")).isEqualTo(4);
        String reset=call("trace-reset",get("/api/lab4/item/5"),200,output);
        assertThat(reset).contains("depth=0").doesNotContain("depth=1");
        results.put("checksPassed",true);
        results.put("capturedAt",java.time.Instant.now().toString());
        results.put("javaVersion",System.getProperty("java.version"));
        results.put("source","Spring Boot integration tests using MockMvc and actual proxy beans");
        mapper.writerWithDefaultPrettyPrinter().writeValue(folder.resolve("results.json").toFile(),results);
    }
}
