package com.ai_engineering.auth_service.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai_engineering.auth_service.DTO.Login.LoginRequest;
import com.ai_engineering.auth_service.DTO.Login.LoginResponse;
import com.ai_engineering.auth_service.DTO.SignUp.SignUpRequest;
import com.ai_engineering.auth_service.Service.AuthService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service){
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signup(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.ok(service.signUp(request));
    }

}
