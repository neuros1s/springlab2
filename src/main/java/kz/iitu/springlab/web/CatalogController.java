package kz.iitu.springlab.web;
import java.util.List;
import java.util.Map;
import kz.iitu.springlab.service.CatalogService;
import kz.iitu.springlab.service.FixedRemovalService;
import org.springframework.aop.support.AopUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/lab4")
public class CatalogController {
    private final CatalogService catalog;
    private final FixedRemovalService fixed;
    public CatalogController(CatalogService catalog, FixedRemovalService fixed) {
        this.catalog = catalog; this.fixed = fixed;
    }
    @GetMapping("/item/{id}")
    public String item(@PathVariable long id) { return catalog.findById(id); }
    @GetMapping("/items")
    public List<String> items(@RequestParam(defaultValue="5") int limit) {
        return catalog.findAll(limit);
    }
    @DeleteMapping("/item/{id}")
    public String remove(@PathVariable long id) { return catalog.remove(id); }
    @GetMapping("/proxy")
    public Map<String,String> proxy() {
        return Map.of("className", catalog.getClass().getName(),
            "superClass", catalog.getClass().getSuperclass().getSimpleName(),
            "isAopProxy", String.valueOf(AopUtils.isAopProxy(catalog)),
            "isCglib", String.valueOf(AopUtils.isCglibProxy(catalog)));
    }
    @GetMapping("/remove-twice/{id}")
    public String beforeFix(@PathVariable long id) { return catalog.removeTwice(id); }
    @GetMapping("/remove-twice-fixed/{id}")
    public String afterFix(@PathVariable long id) { return fixed.removeTwice(id); }
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,String> invalid(IllegalArgumentException ex) {
        return Map.of("error", ex.getClass().getSimpleName(), "message", ex.getMessage());
    }
}
