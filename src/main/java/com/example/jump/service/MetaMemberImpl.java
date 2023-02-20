package com.example.jump.service;

import com.example.jump.domain.ClientSupportApi;
import com.example.jump.domain.RegisterDTO;
import com.example.jump.entity.ClubMember;
import com.example.jump.entity.ClubMemberRole;
import com.example.jump.repository.ClientSupportApiRepository;
import com.example.jump.repository.ClubMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MetaMemberImpl implements MetaMember{

    private final ClientSupportApiRepository clientSupportApiRepository;    // 고객지원 테이블에 접근
    private final ClubMemberRepository clubMemberRepository;    // 회원정보 테이블에 접근

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ClientSupportApi supportSave(String category, String title, String name, String content, String method) {     // 고객지원 데이터 저장
        ClientSupportApi api = new ClientSupportApi(category, title, name, content, method);      // 엔티티 객체 초기화
        this.clientSupportApiRepository.save(api);
        return api;
    }

    public ClubMember getUser(String email) {   // 고객지원에 표시할 회원 정보

        Optional<ClubMember> user = clubMemberRepository.findById(email);

        if (user.isPresent()) {
            return user.get();
        } else {
            return null;
        }
    }

    public boolean register(RegisterDTO registerDTO) {  // 회원가입

        Optional<ClubMember> result = clubMemberRepository.findByEmail(registerDTO.getEmail(), false);

        if (result.isPresent()) {
            return false;
        } else {
            ClubMember clubMember = ClubMember.builder()
                    .email(registerDTO.getEmail())
                    .name(registerDTO.getName())
                    .fromSocial(false)
                    .roleSet(new HashSet<>())
                    .password(passwordEncoder.encode(registerDTO.getPassword()))
                    .build();

            clubMember.addMemberRole(ClubMemberRole.USER);
            clubMemberRepository.save(clubMember);

            return true;
        }
    }
}
