package kz.iitu.springlab.notify;

import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component("email")
@Primary
@Order(2)
public class EmailNotifier implements Notifier {

    public String send(String message) {
        return "email: " + message;
    }

    public String channel() {
        return "email";
    }
}