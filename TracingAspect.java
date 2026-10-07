package kz.iitu.springlab.aspect;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
@Aspect
@Component
@Order(0)
public class TracingAspect {
    private static final Logger log = LoggerFactory.getLogger(TracingAspect.class);
    private final ThreadLocal<Integer> depth = ThreadLocal.withInitial(() -> 0);
    @Before("kz.iitu.springlab.aspect.Pointcuts.serviceOperation()")
    public void enter(JoinPoint jp) {
        int current = depth.get();
        log.info("[TRACE] {}-> {} depth={}", "  ".repeat(current),
            jp.getSignature().toShortString(), current);
        depth.set(current + 1);
    }
    @After("kz.iitu.springlab.aspect.Pointcuts.serviceOperation()")
    public void leave(JoinPoint jp) {
        int current = Math.max(0, depth.get() - 1);
        try {
            log.info("[TRACE] {}<- {} depth={}", "  ".repeat(current),
                jp.getSignature().toShortString(), current);
        } finally {
            if (current == 0) depth.remove();
            else depth.set(current);
        }
    }
}
