package com.leonardo.libraryapi.api.exceptions;

import com.leonardo.libraryapi.exceptions.BusinessException;
import lombok.Getter;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
public class ApiErrors {

    private List<String> errors;

    public ApiErrors(BindingResult bindingResult) {
        errors = new ArrayList<>();
        bindingResult.getAllErrors().forEach(error -> this.errors.add(error.getDefaultMessage()));
    }

    public ApiErrors(BusinessException businessException) {
        errors = Collections.singletonList(businessException.getMessage());
    }

    public ApiErrors(ResponseStatusException responseStatusException) {
        errors = Collections.singletonList(responseStatusException.getReason());
    }
}
