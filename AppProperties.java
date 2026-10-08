package kz.iitu.springlab.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix="app")
public record AppProperties(
        @NotBlank String owner,
        @NotBlank String group,
        @Valid Mail mail,
        @Valid Ui ui
) {
    public record Mail(
            @NotBlank @Email String from,
            @Min(1) @Max(10) int retryCount,
            @DefaultValue("5s") Duration timeout,
            boolean enabled) {}

    public record Ui(
            @NotBlank String theme,
            @Min(5) @Max(50) @DefaultValue("10") int itemsPerPage) {}
}