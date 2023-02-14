package com.example.jump;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableJpaAuditing
@SpringBootApplication  // 스프링부트의 모든 설정이 관리됨.
public class JumpApplication {

    public static void main(String[] args) {
        SpringApplication.run(JumpApplication.class, args);
    }

}
