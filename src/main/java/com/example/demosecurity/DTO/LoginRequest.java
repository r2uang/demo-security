package com.example.demosecurity.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String username;
    private String password;

    private LoginRequest(){
        super();
    }

    public LoginRequest(String username, String password){
        super();
        this.username = username;
        this.password = password;
    }
}
