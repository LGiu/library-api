package com.leonardo.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leonardo.libraryapi.api.BookController;
import com.leonardo.libraryapi.api.dto.BookDto;
import com.leonardo.libraryapi.api.dto.LoanDto;
import com.leonardo.libraryapi.exceptions.BusinessException;
import com.leonardo.libraryapi.model.entity.Book;
import com.leonardo.libraryapi.model.entity.Loan;
import com.leonardo.libraryapi.service.BookService;
import com.leonardo.libraryapi.service.LoanService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

    private static final String BOOK_API = "/api/books";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    private BookDto createNewBook() {
        return BookDto.builder().author("Meu Livro").title("Autor").isbn("123123").build();
    }

    @Test
    @DisplayName("Sucesso - Criar Livro")
    public void createBookTest() throws Exception {

        BookDto bookDto = createNewBook();

        Book savedBook = Book.builder().id(10L).author("Meu Livro").title("Autor").isbn("123123").build();
        BDDMockito.given(bookService.save(Mockito.any(Book.class))).willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(bookDto);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(mockHttpServletRequestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value("10"))
                .andExpect(jsonPath("title").value(bookDto.getTitle()))
                .andExpect(jsonPath("author").value(bookDto.getAuthor()))
                .andExpect(jsonPath("isbn").value(bookDto.getIsbn()));
    }

    @Test
    @DisplayName("Erro - Criar Livro inválido")
    public void createInvalidBookTest() throws Exception {
        String json = new ObjectMapper().writeValueAsString(new BookDto());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(mockHttpServletRequestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(3)));

    }

    @Test
    @DisplayName("Erro - Criar Livro com Isbn Duplicado")
    public void createBookWithDuplicatedTest() throws Exception {
        BookDto bookDto = createNewBook();

        String json = new ObjectMapper().writeValueAsString(bookDto);

        String message = "Isbn já cadastrado.";
        BDDMockito.given(bookService.save(Mockito.any(Book.class))).willThrow(new BusinessException(message));

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(mockHttpServletRequestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(message));

    }

    @Test
    @DisplayName("Sucesso - Busca informações do livro")
    public void getBookDetaisTest() throws Exception {
        Long id = 10L;

        Book book = Book.builder().id(id).author("Meu Livro").title("Autor").isbn("123123").build();

        BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(book));

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc
                .perform(mockHttpServletRequestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(book.getTitle()))
                .andExpect(jsonPath("author").value(book.getAuthor()))
                .andExpect(jsonPath("isbn").value(book.getIsbn()));

    }

    @Test
    @DisplayName("Erro - Retorna Not Found ao busca livro inexistente")
    public void getBookNotFoundTest() throws Exception {
        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 15))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc
                .perform(mockHttpServletRequestBuilder)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Sucesso - Deletar livro")
    public void deleteBookTest() throws Exception {
        Long id = 17L;

        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder().id(id).build()));

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc
                .perform(mockHttpServletRequestBuilder)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Erro - Retorna Not Found ao tentar deletar livro inexistente")
    public void deleteBookNotFoundTest() throws Exception {
        Long id = 17L;

        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc
                .perform(mockHttpServletRequestBuilder)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Sucesso - Atualiza livro")
    public void updateBookTest() throws Exception {
        Long id = 17L;

        Book bookUpdated = Book.builder().id(id).author("Meu Livro").title("Autor").isbn("123123").build();

        String json = new ObjectMapper().writeValueAsString(bookUpdated);

        Book bookUpdating = Book.builder().id(id).title("Título qualquel").author("Autor qualquer").isbn("123123").build();

        BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(bookUpdating));

        BDDMockito.given(bookService.update(bookUpdating)).willReturn(bookUpdated);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc
                .perform(mockHttpServletRequestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(bookUpdated.getTitle()))
                .andExpect(jsonPath("author").value(bookUpdated.getAuthor()))
                .andExpect(jsonPath("isbn").value(bookUpdated.getIsbn()));
    }

    @Test
    @DisplayName("Erro - Retorna Not Found ao tentar atualizar livro inexistente")
    public void updateBookNotFoundTest() throws Exception {
        BookDto book = createNewBook();

        String json = new ObjectMapper().writeValueAsString(book);

        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc
                .perform(mockHttpServletRequestBuilder)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Sucesso - Filtra livros")
    public void getBookFilterTest() throws Exception {
        Long id = 10L;

        Book book = Book.builder().id(id).author("Meu Livro").title("Autor").isbn("123123").build();

        BDDMockito.given(bookService.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<>(Collections.singletonList(book), PageRequest.of(0, 100), 1));

        String query = "?title=" + book.getTitle() + "&atuhor=" + book.getAuthor() + "&page=0&size=100";

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .get(BOOK_API.concat(query))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc
                .perform(mockHttpServletRequestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", Matchers.hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }

    @Test
    @DisplayName("Sucesso - Busca empréstimo por livro")
    public void getLoanByBookTest() throws Exception {
        Book book = Book.builder().id(1L).author("Meu Livro").title("Autor").isbn("123123").build();
        Loan loan = Loan.builder().id(8L).customer("Eu").book(book).build();
        book.setLoans(Collections.singletonList(loan));

        BDDMockito.given(bookService.getById(1L)).willReturn(Optional.of(book));

        BDDMockito.given(loanService.getLaonsByBook(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<>(Collections.singletonList(loan), PageRequest.of(0, 100), 1));

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1 + "/loans"))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc
                .perform(mockHttpServletRequestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", Matchers.hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1));
    }
}
