package kz.iitu.springlab;
import kz.iitu.springlab.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import static org.assertj.core.api.Assertions.assertThat;
class ConfigurationValidationTest {
    @Configuration(proxyBeanMethods=false)
    @EnableConfigurationProperties(AppProperties.class)
    static class Config {}
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(Config.class)
        .withPropertyValues("app.owner=Turgynbek Amirbek", "app.group=IS2413",
            "app.mail.from=no-reply@iitu.kz", "app.mail.retry-count=3", "app.ui.theme=LIGHT", "app.ui.items-per-page=10");
    @Test void validConfigurationBinds() {
        runner.run(c -> { assertThat(c).hasNotFailed(); assertThat(c.getBean(AppProperties.class).mail().retryCount()).isEqualTo(3); });
    }
    @Test void excessiveRetriesAreRejected() {
        runner.withPropertyValues("app.mail.retry-count=99").run(c -> assertThat(c).hasFailed());
    }
    @Test void unsupportedThemeIsRejected() {
        runner.withPropertyValues("app.ui.theme=BLUE").run(c -> assertThat(c).hasFailed());
    }
    @Test void tooSmallPageIsRejected() {
        runner.withPropertyValues("app.ui.items-per-page=4").run(c -> assertThat(c).hasFailed());
    }
}
