package com.deBijenkorf.imageresizeservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
@Slf4j
public class NoResourceFoundException extends RuntimeException {

    public NoResourceFoundException(String message) {
        super(message);
    }
}
