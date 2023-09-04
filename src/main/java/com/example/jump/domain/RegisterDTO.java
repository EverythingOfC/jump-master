package com.example.jump.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class RegisterDTO {  // 등록 DTO
    private String email;
    private String password;
    private String name;

    private Boolean root;   // 관리자 권한
}
