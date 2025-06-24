package com.safetynet.AppSafetyNet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class ErrorSystemException extends RuntimeException{
    public ErrorSystemException(String message){
        super(message);
    }
}
