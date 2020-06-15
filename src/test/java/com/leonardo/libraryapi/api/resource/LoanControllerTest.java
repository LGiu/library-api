package com.leonardo.libraryapi.api.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leonardo.libraryapi.api.dto.BookDto;
import com.leonardo.libraryapi.api.LoanController;
import com.leonardo.libraryapi.api.dto.LoanDto;
import com.leonardo.libraryapi.exceptions.BusinessException;
import com.leonardo.libraryapi.model.entity.Book;
import com.leonardo.libraryapi.model.entity.Loan;
import com.leonardo.libraryapi.service.BookService;
import com.leonardo.libraryapi.service.LoanService;
import com.leonardo.libraryapi.service.LoanServiceTest;
import com.leonardo.libraryapi.service.impl.BookServiceImpl;
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
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

    private static final String LOAN_API = "/api/loans";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loadService;

    private LoanDto createLoan() {
        return LoanDto.builder().isbn("123654").customer("Fulano").build();
    }

    @Test
    @DisplayName("Sucesso - Realizar empréstimo Livro")
    public void createLoanTest() throws Exception {
        LoanDto loadDto = createLoan();

        String json = new ObjectMapper().writeValueAsString(loadDto);

        Book book = Book.builder().id(10L).isbn(loadDto.getIsbn()).build();
        BDDMockito.given(bookService.getBookByIsbn(loadDto.getIsbn()))
                .willReturn(Optional.of(book));

        Loan loan = Loan.builder().id(11L).customer("Fulano").book(book).date(LocalDate.now()).returned(false).build();
        BDDMockito.given(loadService.save(Mockito.any(Loan.class))).willReturn(loan);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(mockHttpServletRequestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(11));
    }

    @Test
    @DisplayName("Erro - Retorna erro ao tentar fazer empréstimo de Livro inexistente")
    public void createInvalidLoanTest() throws Exception {
        LoanDto loadDto = createLoan();

        String json = new ObjectMapper().writeValueAsString(loadDto);

        BDDMockito.given(bookService.getBookByIsbn(loadDto.getIsbn()))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(mockHttpServletRequestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Livro não encontrado para o isbn informado"));
    }

    @Test
    @DisplayName("Erro - Retorna erro ao tentar fazer empréstimo de Livro já empresatado")
    public void loanedBookErrorOnCreateLoanTest() throws Exception {
        LoanDto loadDto = createLoan();

        String json = new ObjectMapper().writeValueAsString(loadDto);

        Book book = Book.builder().id(10L).isbn(loadDto.getIsbn()).build();
        BDDMockito.given(bookService.getBookByIsbn(loadDto.getIsbn()))
                .willReturn(Optional.of(book));

        BDDMockito.given(loadService.save(Mockito.any(Loan.class))).willThrow(new BusinessException("Livro já emprestado"));

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(mockHttpServletRequestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Livro já emprestado"));
    }


    @Test
    @DisplayName("Sucesso - Retornar um livro")
    public void returnBookTest() throws Exception {
        LoanDto loadDto = LoanDto.builder().returned(true).build();

        Loan loan = Loan.builder().id(1L).build();
        BDDMockito.given(loadService.getById(Mockito.anyLong())).willReturn(Optional.of(loan));

        String json = new ObjectMapper().writeValueAsString(loadDto);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .patch(LOAN_API.concat("/1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isOk());

        Mockito.verify(loadService, Mockito.times(1)).update(loan);
    }

    @Test
    @DisplayName("Sucesso - Retornar Not Found quando tenta devolver um livro inexistente")
    public void returnInexistentBookTest() throws Exception {
        LoanDto loadDto = LoanDto.builder().returned(true).build();

        BDDMockito.given(loadService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        String json = new ObjectMapper().writeValueAsString(loadDto);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .patch(LOAN_API.concat("/1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isNotFound());

        Mockito.verify(loadService, Mockito.never()).update(Loan.builder().id(1L).build());
    }

    @Test
    @DisplayName("Sucesso - Filtra livros")
    public void getBookFilterTest() throws Exception {
        Long id = 10L;

        Loan loan = LoanServiceTest.createNewLoan();
        loan.setId(id);

        BDDMockito.given(loadService.find(Mockito.any(LoanDto.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<>(Collections.singletonList(loan), PageRequest.of(0, 100), 1));

        String query = "?isbn=" + loan.getBook().getIsbn() + "&customer=" + loan.getCustomer() + "&page=0&size=100";

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders
                .get(LOAN_API.concat(query))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc
                .perform(mockHttpServletRequestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", Matchers.hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));

    }
}
