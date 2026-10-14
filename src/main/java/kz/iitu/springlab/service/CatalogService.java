package kz.iitu.springlab.service;
import java.util.List;
import java.util.stream.IntStream;
import kz.iitu.springlab.audit.Audited;
import org.springframework.stereotype.Service;
@Service
public class CatalogService {
    public String findById(long id) {
        sleep(50);
        return "Item no. " + id;
    }
    @Audited(action="CATALOG_LIST", logArguments=true)
    public List<String> findAll(int limit) {
        if (limit < 1 || limit > 100) throw new IllegalArgumentException("Limit must be 1..100");
        sleep(300);
        return IntStream.rangeClosed(1, limit).mapToObj(i -> "Item no. " + i).toList();
    }
    @Audited(action="CATALOG_REMOVE")
    public String remove(long id) {
        if (id <= 0) throw new IllegalArgumentException("Invalid identifier: " + id);
        return "Removed item no. " + id;
    }
    public String removeTwice(long id) {
        String first = this.remove(id);
        String second = this.remove(id + 1);
        return first + "; " + second;
    }
    private void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Operation interrupted", ex);
        }
    }
}
