package kz.iitu.springlab.web;
import kz.iitu.springlab.notify.*;
import kz.iitu.springlab.scope.TicketOffice;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/lab2")
public class Lab2Controller {
    private final NotificationService service;
    private final TicketOffice office;
    private final Notifier upper;
    public Lab2Controller(NotificationService service, TicketOffice office, @Qualifier("upper") Notifier upper) {
        this.service=service; this.office=office; this.upper=upper;
    }
    @GetMapping("/notify")
    public Map<String,Object> notify(@RequestParam(defaultValue="hello") String message) {
        return Map.of("primary", service.viaPrimary(message), "console", service.viaConsole(message),
            "all", service.viaAll(message), "beanNames", service.names());
    }
    @GetMapping("/custom")
    public Map<String,String> custom(@RequestParam(defaultValue="hello") String message) {
        return Map.of("channel", upper.channel(), "result", upper.send(message));
    }
    @GetMapping("/scopes") public Map<String,String> scopes() { return office.demonstrate(); }
    @GetMapping("/lifecycle") public Map<String,String> lifecycle() {
        return Map.of("instructions", "Check startup for PostConstruct and graceful shutdown for PreDestroy.");
    }
}
