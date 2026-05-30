package com.ai_engineering.auth_service.DTO.SignUp;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class SignUpRequest {
    
    private String userName;
    private String email;
    private String password;


    public String getUserName(){
        if(userName == null){
            String newUserName = email.split("@")[0];
            this.userName = newUserName;
        }
        return userName;
    }
}
