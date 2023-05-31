package com.example.jump.domain;

import lombok.Data;

@Data
public class RegisterDTO {  // 등록 DTO
    private String email;
    private String password;
    private String name;
    private String root;
}
