package com.ai_engineering.auth_service.DTO.SignUp;

import org.springframework.stereotype.Component;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
@Getter
@Setter
@NoArgsConstructor
public class SignUpRequest {
    
    private String userName;

    @NotBlank(message = "Email is Required")
    @Email(message = "Invalid Email")
    private String email;

    @NotBlank(message = "Password is Required")
    @Size(min = 3,max = 12,message = "Password should be within 3-12 letters")
    private String password;

    public SignUpRequest(String email, String password){
        this.email = email;
        this.password = password;
        this.userName = email.split("@").toString();
    }
    
}
