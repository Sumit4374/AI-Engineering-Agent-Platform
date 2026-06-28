package com.ai_engineering.auth_service.DTO.SignUp;

import org.springframework.stereotype.Component;


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

    public SignUpRequest(String userName, String email, String password){
        this.email = email;
        this.password = password;
        if(userName == null){
            String newUserName = email.split("@")[0];
            this.userName = newUserName;
        }else{
            this.userName = userName;
        }
    }
    
}
