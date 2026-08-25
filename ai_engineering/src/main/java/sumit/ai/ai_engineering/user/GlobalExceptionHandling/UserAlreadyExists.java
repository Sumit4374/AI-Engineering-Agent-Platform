package com.ai_engineering.auth_service.GlobalExceptionHandling;


import lombok.experimental.StandardException;

@StandardException
public class UserAlreadyExists extends RuntimeException {

    public UserAlreadyExists(String msg) {
        super(msg);
    }
    
}
