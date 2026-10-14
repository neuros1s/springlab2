package kz.iitu.springlab.web;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/books", produces = MediaType.APPLICATION_JSON_VALUE)
public class BookRestController {
    private final BookService service;

    public BookRestController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "en")
            String language) {
        if (limit < 0) throw new IllegalArgumentException("Limit must not be negative");
        Locale locale = Locale.lookup(Locale.LanguageRange.parse(language),
                List.of(Locale.ENGLISH, Locale.forLanguageTag("ru")));
        String selected = locale == null ? "en" : locale.getLanguage();
        List<Map<String, Object>> books = service.findAll(author).stream().limit(limit)
                .map(book -> fields(book, selected)).toList();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_LANGUAGE, selected)
                .header(HttpHeaders.VARY, HttpHeaders.ACCEPT_LANGUAGE).body(books);
    }

    private Map<String, Object> fields(Book book, String language) {
        if ("ru".equals(language)) {
            return Map.of("id", book.id(), "название", book.title(),
                    "автор", book.author(), "год", book.year());
        }
        return Map.of("id", book.id(), "title", book.title(),
                "author", book.author(), "year", book.year());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> find(@PathVariable("id") long id) {
        return service.findById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Book> create(@RequestBody Book book) {
        Book saved = service.create(book);
        return ResponseEntity.created(URI.create("/api/books/" + saved.id())).body(saved);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Book> replace(@PathVariable("id") long id,
                                        @RequestBody Book book) {
        return service.replace(id, book).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        return service.delete(id) ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badInput(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
