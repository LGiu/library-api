package com.leonardo.libraryapi.service;

import com.leonardo.libraryapi.api.dto.LoanDto;
import com.leonardo.libraryapi.exceptions.BusinessException;
import com.leonardo.libraryapi.model.entity.Book;
import com.leonardo.libraryapi.model.entity.Loan;
import com.leonardo.libraryapi.model.repository.BookRepository;
import com.leonardo.libraryapi.model.repository.LoanRepository;
import com.leonardo.libraryapi.service.impl.BookServiceImpl;
import com.leonardo.libraryapi.service.impl.LoanServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    private BookService bookService;

    private LoanService loanService;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private LoanRepository loanRepository;

    @BeforeEach
    public void setUp() {
        bookService = new BookServiceImpl(bookRepository);
        loanService = new LoanServiceImpl(loanRepository);
    }

    private Book createNewBook() {
        return Book.builder().title("Meu Livro").author("Autor").isbn("123123").build();
    }

    @Test
    @DisplayName("Sucesso - Salvar um livro")
    public void saveTest() {
        Book book = createNewBook();

        Mockito.when(bookRepository.save(book)).thenReturn(Book.builder().id(11L).author("Autor").title("Meu Livro").isbn("123123").build());

        Book savedBook = bookService.save(book);

        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getTitle()).isEqualTo(book.getTitle());
        assertThat(savedBook.getAuthor()).isEqualTo(book.getAuthor());
        assertThat(savedBook.getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Erro - Criar Livro com Isbn Duplicado")
    public void createBookWithDuplicatedTest() {
        Book book = createNewBook();

        Mockito.when(bookRepository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        Throwable throwable = Assertions.catchThrowable(() -> bookService.save(book));

        assertThat(throwable)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado");

        Mockito.verify(bookRepository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Sucesso - Busca um livro por id")
    public void getByIdTest() {
        Long id = 20L;

        Book book = createNewBook();
        book.setId(id);
        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        Optional<Book> foundBook = bookService.getById(id);

        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(book.getId());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Erro - Retorna Not Found as buscar um livro por id inexistente")
    public void getByIdNotFoundTest() {
        Long id = 20L;

        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Book> book = bookService.getById(id);

        assertThat(book.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Sucesso - Deletar livro")
    public void deleteTest() {
        Book book = createNewBook();
        book.setId(10L);
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> bookService.delete(book));

        Mockito.verify(bookRepository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Erro - Retorna erro ao tentar deletar livro inexistente")
    public void deleteInvalidTest() {
        Book book = createNewBook();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> bookService.delete(book));

        Mockito.verify(bookRepository, Mockito.never()).delete(book);
    }


    @Test
    @DisplayName("Sucesso - Atualiza livro")
    public void updateTest() {
        Long id = 12L;

        Book bookUpdating = Book.builder().id(id).build();

        Book bookUpdated = createNewBook();
        bookUpdated.setId(id);

        Mockito.when(bookRepository.save(bookUpdating)).thenReturn(bookUpdated);

        Book book = bookService.update(bookUpdating);

        assertThat(book.getId()).isEqualTo(bookUpdated.getId());
        assertThat(book.getTitle()).isEqualTo(bookUpdated.getTitle());
        assertThat(book.getIsbn()).isEqualTo(bookUpdated.getIsbn());
        assertThat(book.getAuthor()).isEqualTo(bookUpdated.getAuthor());
    }

    @Test
    @DisplayName("Erro - Retorna erro ao tentar atualizar livro inexistente")
    public void updateInvalidTest() {
        Book book = createNewBook();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> bookService.update(book));

        Mockito.verify(bookRepository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Sucesso - Filtrar livros")
    public void getFilterTest() {
        Book book = createNewBook();

        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Book> page = new PageImpl<>(Collections.singletonList(book), pageRequest, 1);

        Mockito.when(bookRepository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Book> pageResult = bookService.find(book, pageRequest);


        assertThat(pageResult.getTotalElements()).isEqualTo(1);
        assertThat(pageResult.getContent()).isEqualTo(Collections.singletonList(book));
        assertThat(pageResult.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(pageResult.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Sucesso - Busca livro por isbn")
    public void getBookByIsbnTest() {
        String isbn = "5122";

        Book book = createNewBook();
        book.setIsbn(isbn);
        Mockito.when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(book));

        Optional<Book> bookResult = bookService.getBookByIsbn(isbn);

        assertThat(bookResult.isPresent()).isTrue();
        assertThat(bookResult.get().getIsbn()).isEqualTo(isbn);
        Mockito.verify(bookRepository, Mockito.times(1)).findByIsbn(isbn);
    }

    @Test
    @DisplayName("Sucesso - Busca empréstimos por livro")
    public void getLoansByBookTest() {
        Book book = createNewBook();
        book.setId(14L);

        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Loan> loans = Collections.singletonList(Loan.builder().id(8L).customer("Eu").book(book).build());
        Page<Loan> page = new PageImpl<>(loans, pageRequest, 1);

        Mockito.when(loanRepository.findByBook(Mockito.any(Book.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Loan> pageResult = loanService.getLaonsByBook(book, pageRequest);

        assertThat(pageResult.getTotalElements()).isEqualTo(1);
        assertThat(pageResult.getContent()).isEqualTo(loans);
        assertThat(pageResult.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(pageResult.getPageable().getPageSize()).isEqualTo(10);
    }
}
