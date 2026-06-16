package com.ai_engineering.auth_service.Service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ai_engineering.auth_service.Configuration.CustomUserDetails.CustomUserDetails;
import com.ai_engineering.auth_service.DTO.Login.LoginRequest;
import com.ai_engineering.auth_service.DTO.Login.LoginResponse;
import com.ai_engineering.auth_service.DTO.SignUp.SignUpRequest;
import com.ai_engineering.auth_service.JWT.JwtService;
import com.ai_engineering.auth_service.Model.User;
import com.ai_engineering.auth_service.Repository.UserRepo;


@Service
public class AuthService {
    
    private final UserRepo repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthService(UserRepo repo, PasswordEncoder encoder, AuthenticationManager authManager, JwtService jwtService){
        this.encoder = encoder;
        this.repo = repo;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    public LoginResponse signUp(SignUpRequest request){
        if(repo.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email Already Exist");
        }
        if(repo.existsByUserName(request.getUserName())){
            throw new RuntimeException("UserName Already Taken");
        }
        try {
            User newUser = User.builder().email(request.getEmail())
                            .userName(request.getUserName())
                            .email(request.getEmail())
                            .password(
                                encoder.encode(request.getPassword()).toString()
                            )
                            .role(request.getRole()).build();
            repo.save(newUser);
            LoginRequest loginRequest = new LoginRequest(request.getEmail(), request.getPassword());
            return login(loginRequest);
        } catch (Exception e) {
            throw new RuntimeException(request.getPassword() + "  ........   " + e.getMessage());
        }
    }

    public LoginResponse login(LoginRequest request){
        Authentication authentication = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
        );
        CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        return LoginResponse.builder()
        .userId(userDetails.getId())
        .userName(userDetails.getEmail())
        .email(userDetails.getUsername())
        .token(token)
        .role(userDetails.getRole())
        .build();
    }
}
