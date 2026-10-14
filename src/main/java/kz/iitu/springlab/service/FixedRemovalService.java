package kz.iitu.springlab.service;
import org.springframework.stereotype.Service;
@Service
public class FixedRemovalService {
    private final CatalogService catalog;
    public FixedRemovalService(CatalogService catalog) { this.catalog = catalog; }
    public String removeTwice(long id) {
        String first = catalog.remove(id);
        String second = catalog.remove(id + 1);
        return first + "; " + second;
    }
}
