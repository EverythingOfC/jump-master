package com.example.jump.config;


import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.ui.Model;

@Configuration
@Log4j2
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests((auth) -> {
            auth.antMatchers("/sample/all").permitAll();
            auth.antMatchers("/sample/member").hasRole("USER");
        });

        //http.formLogin();
        http.formLogin().loginPage("/").loginProcessingUrl("/login_post").defaultSuccessUrl("/index");
        http.csrf().disable();
        http.logout();

        return http.build();
    }
}
