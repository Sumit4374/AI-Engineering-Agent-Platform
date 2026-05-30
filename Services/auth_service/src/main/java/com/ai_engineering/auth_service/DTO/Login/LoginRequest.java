package com.ai_engineering.auth_service.DTO.Login;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class LoginRequest {
    
    private String userNameOrEmail;

    private String password;
}
