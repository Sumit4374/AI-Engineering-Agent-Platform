package com.ai_engineering.auth_service.DTO.SignUp;

import org.springframework.stereotype.Component;

import com.ai_engineering.auth_service.Model.Enum.Role;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
@Getter
@Setter
@NoArgsConstructor
public class SignUpRequest {
    
    private String userName;
    private String email;
    private String password;
    private Role role;

    public SignUpRequest(String userName, String email, String password, Role role){
        this.email = email;
        this.password = password;
        if(userName == null){
            String newUserName = email.split("@")[0];
            this.userName = newUserName;
        }else{
            this.userName = userName;
        }
        this.role = role;
    }
    
}
