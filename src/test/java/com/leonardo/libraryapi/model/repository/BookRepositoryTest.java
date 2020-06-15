package com.leonardo.libraryapi.model.repository;

import com.leonardo.libraryapi.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private BookRepository bookRepository;

    private Book createNewBook() {
        return Book.builder().author("Meu Livro").title("Autor").isbn("123123").build();
    }

    @Test
    @DisplayName("Sucesso - Salva livro")
    public void saveTest() {
        Book book = createNewBook();
        testEntityManager.persist(book);

        assertThat(book.getId()).isNotNull();
    }

    @Test
    @DisplayName("Sucesso - Retorna verdadeiro se existe livro na base com isbn informado")
    public void returnTrueWhenisbnExistsTest() {
        Book book = createNewBook();

        testEntityManager.persist(book);

        boolean exist = bookRepository.existsByIsbn(book.getIsbn());

        assertThat(exist).isTrue();
    }

    @Test
    @DisplayName("Sucesso - Retorna false se não existe livro na base com isbn informado")
    public void returnFalseWhenisbnExistsTest() {
        Book book = createNewBook();

        boolean exist = bookRepository.existsByIsbn(book.getIsbn());

        assertThat(exist).isFalse();
    }

    @Test
    @DisplayName("Sucesso - Deletar livro")
    public void deleteTest() {
        Book book = createNewBook();
        testEntityManager.persist(book);

        bookRepository.delete(book);

        Optional<Book> foundBook = bookRepository.findById(book.getId());

        assertThat(foundBook.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Sucesso - Atualiza livro")
    public void updateTest() {
        Book book = createNewBook();
        testEntityManager.persist(book);

        book.setTitle("Título novo");
        bookRepository.save(book);

        Optional<Book> foundBook = bookRepository.findById(book.getId());

        assertThat(foundBook.isPresent()).isTrue();
        assertThat(book.getTitle()).isEqualTo("Título novo");
    }

    @Test
    @DisplayName("Sucesso - Busca livro por isbn")
    public void findByIsbnTest() {
        Book book = createNewBook();

        testEntityManager.persist(book);

        Optional<Book> foundBook = bookRepository.findByIsbn(book.getIsbn());

        assertThat(foundBook.isPresent()).isTrue();
        assertThat(book.getIsbn()).isEqualTo(book.getIsbn());
    }
}
