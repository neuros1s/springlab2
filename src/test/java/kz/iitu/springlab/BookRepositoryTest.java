package kz.iitu.springlab;

import kz.iitu.springlab.catalog.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class BookRepositoryTest {
    @Test void creationAssignsANewIdAndDoesNotOverwriteAClientSuppliedId() {
        BookRepository repository = new BookRepository();
        BookService service = new BookService(repository);
        Book created = service.create(new Book(1L, "New Book", "New Author", 2024));
        assertThat(created.id()).isEqualTo(4L);
        assertThat(repository.findById(1).orElseThrow().title()).isEqualTo("Effective Java");
        assertThat(repository.findAll()).hasSize(4);
    }

    @Test void replacementUsesThePathIdAndDoesNotInsertAnAbsentBook() {
        BookRepository repository = new BookRepository();
        BookService service = new BookService(repository);
        Book input = new Book(99L, "Revised", "Author", 2024);
        assertThat(service.replace(1, input).orElseThrow().id()).isEqualTo(1L);
        assertThat(service.replace(999, input)).isEmpty();
        assertThat(repository.findAll()).hasSize(3);
        assertThat(repository.findById(999)).isEmpty();
    }

    @Test void deletionIsIdempotentAndTheListCannotMutateTheRepository() {
        BookRepository repository = new BookRepository();
        assertThat(repository.deleteById(2)).isTrue();
        assertThat(repository.deleteById(2)).isFalse();
        assertThat(repository.findAll()).extracting(Book::id).containsExactly(1L, 3L);
        assertThatThrownBy(() -> repository.findAll().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
