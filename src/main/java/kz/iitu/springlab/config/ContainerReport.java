package kz.iitu.springlab.config;
import kz.iitu.springlab.notify.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
@Component
public class ContainerReport implements CommandLineRunner {
    private final ApplicationContext context;
    public ContainerReport(ApplicationContext context) { this.context=context; }
    @Override public void run(String... args) {
        System.out.println("Bean count: " + context.getBeanDefinitionCount());
        context.getBeansOfType(Notifier.class).forEach((name,bean) -> System.out.println(name+": "+bean.getClass().getName()));
        System.out.println("NotificationService: "+context.getBean(NotificationService.class).getClass().getName());
    }
}
