package kz.iitu.springlab.config;

import org.springframework.context.annotation.*;
import java.time.format.DateTimeFormatter;

@Configuration
public class FormatConfig {

    @Bean
    public DateTimeFormatter reportDateFormatter() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }
}