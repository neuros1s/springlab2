package kz.iitu.springlab.lifecycle;

import jakarta.annotation.*;
import org.springframework.stereotype.Component;

@Component
public class LifecycleDemo {

    @PostConstruct
    void init() {
        System.out.println("Lifecycle started");
    }

    @PreDestroy
    void destroy() {
        System.out.println("Lifecycle stopped");
    }
}