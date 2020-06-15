package com.leonardo.libraryapi.service;

import com.leonardo.libraryapi.api.dto.BookDto;
import com.leonardo.libraryapi.api.dto.LoanDto;
import com.leonardo.libraryapi.exceptions.BusinessException;
import com.leonardo.libraryapi.model.entity.Book;
import com.leonardo.libraryapi.model.entity.Loan;
import com.leonardo.libraryapi.model.repository.BookRepository;
import com.leonardo.libraryapi.model.repository.LoanRepository;
import com.leonardo.libraryapi.service.impl.BookServiceImpl;
import com.leonardo.libraryapi.service.impl.LoanServiceImpl;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    private LoanService loanService;

    @MockBean
    private LoanRepository loanRepository;

    @BeforeEach
    public void setUp() {
        loanService = new LoanServiceImpl(loanRepository);
    }

    private static Book createNewBook() {
        return Book.builder().id(10L).title("Meu Livro").author("Autor").isbn("123123").build();
    }

    public static Loan createNewLoan() {
        return Loan.builder().book(createNewBook()).customer("Ciclano").date(LocalDate.now()).build();
    }

    @Test
    @DisplayName("Sucesso - Salvar um empréstimo")
    public void saveTest() {
        Loan loanSaving = createNewLoan();
        Loan loanSaved = createNewLoan();
        loanSaved.setId(11L);

        Mockito.when(loanRepository.existsByBookAndNotReturned(createNewBook())).thenReturn(false);
        Mockito.when(loanRepository.save(loanSaving)).thenReturn(loanSaved);

        Loan loanResult = loanService.save(loanSaving);

        assertThat(loanResult.getId()).isNotNull();
        assertThat(loanResult.getCustomer()).isEqualTo(loanSaved.getCustomer());
        assertThat(loanResult.getDate()).isEqualTo(loanSaved.getDate());
        assertThat(loanResult.getBook().getId()).isEqualTo(loanSaved.getBook().getId());
    }

    @Test
    @DisplayName("Erro - Retorna erro ao tentar salva com um livro já emprestado")
    public void saveInvalidTest() {
        Loan loan = createNewLoan();

        Mockito.when(loanRepository.existsByBookAndNotReturned(createNewBook())).thenReturn(true);

        Throwable throwable = catchThrowable(() -> loanService.save(loan));

        assertThat(throwable).isInstanceOf(BusinessException.class)
                .hasMessage("Livro já emprestado");

        Mockito.verify(loanRepository, Mockito.never()).save(loan);
    }

    @Test
    @DisplayName("Sucesso - Busca empréstimo por id")
    public void getByIdTest() {
        Loan loan = createNewLoan();
        loan.setId(10L);

        Mockito.when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));

        Optional<Loan> loanResult = loanService.getById(loan.getId());

        assertThat(loanResult.isPresent()).isTrue();
        assertThat(loanResult.get().getId()).isEqualTo(loan.getId());
        assertThat(loanResult.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(loanResult.get().getReturned()).isEqualTo(loan.getReturned());
        assertThat(loanResult.get().getBook().getId()).isEqualTo(loan.getBook().getId());

        Mockito.verify(loanRepository).findById(loan.getId());
    }



    @Test
    @DisplayName("Sucesso - Atualiza empréstimo")
    public void updateTest() {
        Long id = 12L;
        Loan loandUpdating = Loan.builder().id(id).build();

        Loan loandUpdated = createNewLoan();
        loandUpdated.setId(id);

        Mockito.when(loanRepository.save(loandUpdating)).thenReturn(loandUpdated);

        Loan loan = loanService.update(loandUpdating);

        assertThat(loan.getId()).isEqualTo(loandUpdated.getId());
        assertThat(loan.getCustomer()).isEqualTo(loandUpdated.getCustomer());
        assertThat(loan.getReturned()).isEqualTo(loandUpdated.getReturned());
        assertThat(loan.getDate()).isEqualTo(loandUpdated.getDate());
        assertThat(loan.getBook().getId()).isEqualTo(loandUpdated.getBook().getId());

        Mockito.verify(loanRepository).save(loandUpdating);
    }

    @Test
    @DisplayName("Sucesso - Filtrar empréstimos")
    public void getFilterTest() {
        LoanDto loanDto = LoanDto.builder().book(BookDto.builder().isbn("123123").build()).customer("Ciclano").build();

        Loan loan = createNewLoan();
        loan.setId(7L);

        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Loan> page = new PageImpl<>(Collections.singletonList(loan), pageRequest, 1);

        Mockito.when(loanRepository.findByBookIsbnOrCustomer(Mockito.anyString(), Mockito.anyString(), Mockito.any(PageRequest.class)))
                .thenReturn(page);


        Page<Loan> pageResult = loanService.find(loanDto, pageRequest);

        assertThat(pageResult.getTotalElements()).isEqualTo(1);
        assertThat(pageResult.getContent()).isEqualTo(Collections.singletonList(loan));
        assertThat(pageResult.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(pageResult.getPageable().getPageSize()).isEqualTo(10);
    }

}
