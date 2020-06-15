package com.leonardo.libraryapi.api.dto;

import com.leonardo.libraryapi.model.entity.Loan;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {

    private Long id;

    @NotEmpty
    private String title;

    @NotEmpty
    private String author;

    @NotEmpty
    private String isbn;

    private List<LoanDto> loans;
}
