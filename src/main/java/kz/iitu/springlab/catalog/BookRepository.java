package kz.iitu.springlab.catalog;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepository {
    private final Map<Long, Book> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public BookRepository() {
        save(new Book(null, "Effective Java", "Joshua Bloch", 2018));
        save(new Book(null, "Clean Code", "Robert Martin", 2008));
        save(new Book(null, "Spring in Action", "Craig Walls", 2022));
    }

    public List<Book> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparingLong(Book::id)).toList();
    }

    public Optional<Book> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Book save(Book book) {
        long id = book.id() == null ? sequence.incrementAndGet() : book.id();
        sequence.accumulateAndGet(id, Math::max);
        Book stored = new Book(id, book.title(), book.author(), book.year());
        storage.put(id, stored);
        return stored;
    }

    public Optional<Book> replace(long id, Book book) {
        return Optional.ofNullable(storage.computeIfPresent(id,
                (key, old) -> new Book(key, book.title(), book.author(), book.year())));
    }

    public boolean deleteById(long id) {
        return storage.remove(id) != null;
    }
}
