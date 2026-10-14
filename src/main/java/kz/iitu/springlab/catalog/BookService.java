package kz.iitu.springlab.catalog;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll(String author) {
        String filter = author == null ? "" : author.strip().toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(book -> book.author().toLowerCase(Locale.ROOT).contains(filter))
                .toList();
    }

    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }

    public Book create(Book book) {
        Book checked = validate(book);
        return repository.save(new Book(null, checked.title(), checked.author(), checked.year()));
    }

    public Optional<Book> replace(long id, Book book) {
        return repository.replace(id, validate(book));
    }

    public boolean delete(long id) {
        return repository.deleteById(id);
    }

    private Book validate(Book book) {
        if (book == null || book.title() == null || book.title().isBlank()
                || book.author() == null || book.author().isBlank()) {
            throw new IllegalArgumentException("Title and author are required");
        }
        if (book.year() < 1 || book.year() > 9999) {
            throw new IllegalArgumentException("Year must be between 1 and 9999");
        }
        return new Book(book.id(), book.title().strip(), book.author().strip(), book.year());
    }
}
