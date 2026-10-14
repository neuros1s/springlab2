package kz.iitu.springlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookWebLayerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    private static final String INPUT =
            "{\"id\":99,\"title\":\"Pro Spring 6\",\"author\":\"Cosmina\",\"year\":2023}";

    @Test void fiveRestOperationsAndTheRequiredErrorsHaveCorrectStatuses() throws Exception {
        mvc.perform(get("/api/books")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
        mvc.perform(get("/api/books").param("author", "bloch").param("limit", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Effective Java"));
        mvc.perform(get("/api/books/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        mvc.perform(get("/api/books/999")).andExpect(status().isNotFound());
        var created = mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                .content(INPUT)).andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/books/4"))
                .andExpect(jsonPath("$.id").value(4)).andReturn();
        assertThat(mapper.readTree(created.getResponse().getContentAsString()).get("id").asLong())
                .isEqualTo(4L);
        mvc.perform(put("/api/books/1").contentType(MediaType.APPLICATION_JSON).content(INPUT))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Pro Spring 6"));
        mvc.perform(put("/api/books/999").contentType(MediaType.APPLICATION_JSON).content(INPUT))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/books/2")).andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mvc.perform(delete("/api/books/2")).andExpect(status().isNotFound());
        mvc.perform(get("/api/books/abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/nothing")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/books")).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/api/books").contentType(MediaType.TEXT_PLAIN).content("not JSON"))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(get("/api/books").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable());
        mvc.perform(get("/api/books").param("limit", "abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/books").param("limit", "-1")).andExpect(status().isBadRequest());
    }

    @Test void languageHeaderChangesFieldsAndHonoursRegionalAndWeightedPreferences()
            throws Exception {
        mvc.perform(get("/api/books").param("limit", "1").header("Accept-Language", "ru-RU"))
                .andExpect(status().isOk()).andExpect(header().string("Content-Language", "ru"))
                .andExpect(header().string("Vary", "Accept-Language"))
                .andExpect(jsonPath("$[0]['название']").value("Effective Java"))
                .andExpect(jsonPath("$[0].title").doesNotExist());
        mvc.perform(get("/api/books").param("limit", "1")
                .header("Accept-Language", "en;q=0.2,ru;q=0.9"))
                .andExpect(header().string("Content-Language", "ru"));
        mvc.perform(get("/api/books").param("limit", "1").header("Accept-Language", "en"))
                .andExpect(header().string("Content-Language", "en"))
                .andExpect(jsonPath("$[0].title").value("Effective Java"));
        mvc.perform(get("/api/books").param("limit", "1").header("Accept-Language", "kk"))
                .andExpect(header().string("Content-Language", "en"));
    }

    @Test void viewStaticResourcesAndFlashSurviveOneRedirectWithoutDuplicateCreation()
            throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("index.html"));
        mvc.perform(get("/css/app.css")).andExpect(status().isOk())
                .andExpect(content().string(containsString("border-collapse")));
        mvc.perform(get("/books")).andExpect(status().isOk())
                .andExpect(view().name("books/list")).andExpect(model().attribute("title", "Catalogue"))
                .andExpect(model().attribute("books", hasSize(3)))
                .andExpect(content().string(containsString("Effective Java")))
                .andExpect(content().string(not(containsString("th:each"))));
        mvc.perform(get("/books").param("author", "Bloch")).andExpect(status().isOk())
                .andExpect(model().attribute("books", hasSize(1)))
                .andExpect(content().string(not(containsString("Clean Code"))));
        var post = mvc.perform(post("/books").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Java Precisely").param("author", "Sestoft").param("year", "2016"))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/books")).andReturn();
        MockHttpSession session = (MockHttpSession) post.getRequest().getSession(false);
        assertThat(session).isNotNull();
        mvc.perform(get("/books").session(session)).andExpect(status().isOk())
                .andExpect(model().attribute("message", "Book \"Java Precisely\" has been added"))
                .andExpect(model().attribute("books", hasSize(4)))
                .andExpect(content().string(containsString("has been added")));
        mvc.perform(get("/books").session(session)).andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("message"))
                .andExpect(model().attribute("books", hasSize(4)))
                .andExpect(content().string(not(containsString("has been added"))));
    }
}
