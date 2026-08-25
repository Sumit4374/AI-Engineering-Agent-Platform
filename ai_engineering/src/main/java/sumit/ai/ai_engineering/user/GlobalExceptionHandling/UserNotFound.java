package com.ai_engineering.auth_service.GlobalExceptionHandling;


import lombok.experimental.StandardException;

@StandardException
public class UserNotFound extends RuntimeException {

    public UserNotFound(String msg) {
        super(msg);

    }
    
}
