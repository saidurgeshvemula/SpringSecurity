package com.example.springsec;

import lombok.Data;
@Data
public class RegisterRequest {

        private String username;
        private String password;
        private Role role;
}
