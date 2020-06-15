package com.leonardo.libraryapi.model.repository;

import com.leonardo.libraryapi.model.entity.Book;
import com.leonardo.libraryapi.model.entity.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private LoanRepository loanRepository;

    private Book createNewBook() {
        return Book.builder().author("Meu Livro").title("Autor").isbn("123123").build();
    }


    private Loan createNewLoan() {
        return Loan.builder().book(createNewBook()).customer("Ciclano").date(LocalDate.now()).build();
    }

    @Test
    @DisplayName("Sucesso - Verificar se exise empréstimo não devolvido para o livro")
    public void saveTest() {
        Book book = createNewBook();
        testEntityManager.persist(book);

        Loan loan = createNewLoan();
        loan.setBook(book);
        testEntityManager.persist(loan);

        boolean exist = loanRepository.existsByBookAndNotReturned(book);

        assertThat(exist).isTrue();
    }

    @Test
    @DisplayName("Sucesso - Busca livro por isbn ou cliente")
    public void findByBookIsbnOrCustomerTest() {
        Book book = createNewBook();
        testEntityManager.persist(book);

        Loan loan = createNewLoan();
        loan.setBook(book);
        testEntityManager.persist(loan);

        Page<Loan> loanPage = loanRepository.findByBookIsbnOrCustomer(loan.getBook().getIsbn(), loan.getCustomer(), PageRequest.of(0, 10));

        assertThat(loanPage.getContent()).hasSize(1);
        assertThat(loanPage.getContent()).contains(loan);
        assertThat(loanPage.getPageable().getPageSize()).isEqualTo(10);
        assertThat(loanPage.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(loanPage.getTotalElements()).isEqualTo(1);
    }


}
