package com.leonardo.libraryapi.exceptions;

import com.leonardo.libraryapi.model.entity.Book;

public class BusinessException extends RuntimeException {

    public BusinessException(String s) {
        super(s);
    }
}
