package com.leonardo.libraryapi.service.impl;

import com.leonardo.libraryapi.api.dto.LoanDto;
import com.leonardo.libraryapi.exceptions.BusinessException;
import com.leonardo.libraryapi.model.entity.Book;
import com.leonardo.libraryapi.model.entity.Loan;
import com.leonardo.libraryapi.model.repository.LoanRepository;
import com.leonardo.libraryapi.service.BookService;
import com.leonardo.libraryapi.service.LoanService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoanServiceImpl implements LoanService {

    private LoanRepository loanRepository;

    public LoanServiceImpl(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public Loan save(Loan loan) {
        if (loanRepository.existsByBookAndNotReturned(loan.getBook())) {
            throw new BusinessException("Livro já emprestado");
        }
        return loanRepository.save(loan);
    }

    @Override
    public Optional<Loan> getById(Long id) {
        return loanRepository.findById(id);
    }

    @Override
    public Loan update(Loan loan) {
        return loanRepository.save(loan);
    }

    @Override
    public Page<Loan> find(LoanDto loanDto, Pageable pageable) {
        return loanRepository.findByBookIsbnOrCustomer(loanDto.getBook().getIsbn(), loanDto.getCustomer(), pageable);
    }

    @Override
    public Page<Loan> getLaonsByBook(Book book, Pageable pageable) {
        return loanRepository.findByBook(book, pageable);
    }

}
