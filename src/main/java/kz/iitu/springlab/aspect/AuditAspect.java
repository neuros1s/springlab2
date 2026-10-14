package kz.iitu.springlab.aspect;
import java.time.Instant;
import java.util.Arrays;
import kz.iitu.springlab.audit.Audited;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
@Aspect
@Component
@Order(1)
public class AuditAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    @Around(value="@annotation(audited)", argNames="pjp,audited")
    public Object audit(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        String args = audited.logArguments()
            ? " args=" + Arrays.toString(pjp.getArgs()) : "";
        log.info("[AUDIT] start {} at={}{}", audited.action(), Instant.now(), args);
        try {
            Object result = pjp.proceed();
            log.info("[AUDIT] {} success at={}", audited.action(), Instant.now());
            return result;
        } catch (Throwable ex) {
            log.info("[AUDIT] {} failure at={} error={}", audited.action(),
                Instant.now(), ex.getMessage());
            throw ex;
        }
    }
}
