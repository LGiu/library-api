package com.leonardo.libraryapi.service;

import com.leonardo.libraryapi.api.dto.LoanDto;
import com.leonardo.libraryapi.model.entity.Book;
import com.leonardo.libraryapi.model.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LoanService {

    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);

    Page<Loan> find(LoanDto loanDto, Pageable pageable);

    Page<Loan> getLaonsByBook(Book book, Pageable any);
}
