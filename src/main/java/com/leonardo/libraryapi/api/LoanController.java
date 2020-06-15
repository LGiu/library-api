package com.leonardo.libraryapi.api;

import com.leonardo.libraryapi.api.dto.BookDto;
import com.leonardo.libraryapi.api.dto.LoanDto;
import com.leonardo.libraryapi.model.entity.Book;
import com.leonardo.libraryapi.model.entity.Loan;
import com.leonardo.libraryapi.service.BookService;
import com.leonardo.libraryapi.service.LoanService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private LoanService loanService;
    private BookService bookService;
    private ModelMapper modelMapper;

    public LoanController(LoanService loanService, BookService bookService, ModelMapper modelMapper) {
        this.loanService = loanService;
        this.bookService = bookService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanDto post(@RequestBody @Valid LoanDto loanDto) {
        Book book = bookService.getBookByIsbn(loanDto.getIsbn())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Livro não encontrado para o isbn informado"));

        Loan loan = modelMapper.map(loanDto, Loan.class);
        loan.setBook(book);
        Loan entity = loanService.save(loan);

        return modelMapper.map(entity, LoanDto.class);
    }

    @PatchMapping("{id}")
    public void patch(@PathVariable Long id, @RequestBody LoanDto loanDto) {
        Loan loan = loanService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro inexistente"));

        loan.setReturned(loanDto.getReturned());
        loanService.update(loan);
    }


    @GetMapping
    public Page<LoanDto> getByFilter(LoanDto loanDto, Pageable pageable) {
        Page<Loan> pageLoan = loanService.find(loanDto, pageable);
        List<LoanDto> list = pageLoan.getContent().stream().map(b -> modelMapper.map(b, LoanDto.class)).collect(Collectors.toList());
        return new PageImpl<>(list, pageable, pageLoan.getTotalElements());
    }
}

