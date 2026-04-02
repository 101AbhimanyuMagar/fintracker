package com.fintracker_backend.fintracker.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {

    private String name;
    private String email;
    private String password;

}
