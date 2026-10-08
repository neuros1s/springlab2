package kz.iitu.springlab.web;

import kz.iitu.springlab.config.*;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/lab3")
public class Lab3Controller {
    private final AppProperties props;
    private final EnvironmentBanner banner;
    private final Environment environment;

    public Lab3Controller(AppProperties props, EnvironmentBanner banner, Environment environment){
        this.props=props;
        this.banner=banner;
        this.environment=environment;
    }

    @GetMapping("/config")
    public Map<String,Object> config(){
        return Map.of(
        "owner", props.owner(),
        "group", props.group(),
        "mailRetryCount", props.mail().retryCount(),
        "mailEnabled", props.mail().enabled(),
        "uiTheme", props.ui().theme(),
        "itemsPerPage", props.ui().itemsPerPage(),
        "serverPort", environment.getProperty("server.port"),
        "activeProfiles", Arrays.asList(environment.getActiveProfiles()),
        "banner", banner.describe());
    }
}