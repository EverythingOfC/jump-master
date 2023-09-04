package com.example.jump.domain;

import lombok.Data;

@Data
public class RegisterDTO {  // 등록 DTO
    private String email;
    private String password;
    private String name;
<<<<<<< HEAD

    private Boolean root;   // 관리자 권한
=======
    private String root;
>>>>>>> 4a1b7f01a5f80c91666df3c4220686c5ea2ae776
}
