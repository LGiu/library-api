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

import java.util.List;

import javax.validation.Valid;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final LoanService loanService;
    private final ModelMapper modelMapper;

    public BookController(BookService bookService, LoanService loanService, ModelMapper modelMapper) {
        this.bookService = bookService;
        this.loanService = loanService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto post(@RequestBody @Valid BookDto bookDto) {
        Book book = modelMapper.map(bookDto, Book.class);
        Book entity = bookService.save(book);
        return modelMapper.map(entity, BookDto.class);
    }

    @GetMapping("{id}")
    public BookDto get(@PathVariable Long id) {
        return bookService
                .getById(id)
                .map(book -> modelMapper.map(book, BookDto.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public Page<BookDto> getByFilter(BookDto bookDto, Pageable pageable) {
        Book bookFilter = modelMapper.map(bookDto, Book.class);
        Page<Book> pageBook = bookService.find(bookFilter, pageable);
        List<BookDto> list = pageBook.getContent().stream().map(b -> modelMapper.map(b, BookDto.class)).collect(Collectors.toList());
        return new PageImpl<>(list, pageable, pageBook.getTotalElements());
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Book book = bookService
                .getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        bookService.delete(book);
    }

    @PutMapping("{id}")
    public BookDto put(@PathVariable Long id, BookDto bookDto) {
        return bookService
                .getById(id)
                .map(book -> {
                    book.setAuthor(bookDto.getAuthor());
                    book.setTitle(bookDto.getTitle());
                    book = bookService.update(book);
                    return modelMapper.map(book, BookDto.class);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("{id}/loans")
    public Page<LoanDto> getLoansByBook(@PathVariable Long id, Pageable pageable) {
        Book book = bookService
                .getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Page<Loan> result = loanService.getLaonsByBook(book, pageable);
        List<LoanDto> list = result.getContent().stream().map(loan -> {
            Book loanBook = loan.getBook();
            loanBook.setLoans(null);
            BookDto bookDto = modelMapper.map(loanBook, BookDto.class);
            LoanDto loanDto = modelMapper.map(loan, LoanDto.class);
            loanDto.setBook(bookDto);
            return loanDto;
        }).collect(Collectors.toList());
        return new PageImpl<>(list, pageable, result.getTotalElements());
    }
}
