package com.example.jump.service;

import com.example.jump.domain.RegisterDTO;
import com.example.jump.entity.ClubMember;
import com.example.jump.entity.ClubMemberRole;
import com.example.jump.repository.ClubMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@Service
public class RegisterService {

    @Autowired
    private ClubMemberRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean register(RegisterDTO registerDTO) {

        Optional<ClubMember> result = repository.findByEmail(registerDTO.getEmail(), false);

        if (result.isPresent()) {
            return  false;
        } else {
            ClubMember clubMember = ClubMember.builder()
                    .email(registerDTO.getEmail())
                    .name(registerDTO.getName())
                    .fromSocial(false)
                    .roleSet(new HashSet<ClubMemberRole>())
                    .password(passwordEncoder.encode(registerDTO.getPassword()))
                    .build();

            clubMember.addMemberRole(ClubMemberRole.USER);
            repository.save(clubMember);
            return true;
        }
    }
}
