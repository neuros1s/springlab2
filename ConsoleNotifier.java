package kz.iitu.springlab.notify;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component("console")
@Order(1)
public class ConsoleNotifier implements Notifier {

    public String send(String message) {
        return "console: " + message;
    }

    public String channel() {
        return "console";
    }
}