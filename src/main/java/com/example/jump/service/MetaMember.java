package com.example.jump.service;

import com.example.jump.domain.ClientSupportApi;
import com.example.jump.domain.RegisterDTO;
import com.example.jump.entity.ClubMember;

public interface MetaMember {   // 회원 관련 인터페이스

    ClientSupportApi supportSave(String category, String title, String name, String content, String method);     // 고객지원 저장
    ClubMember getUser(String email);   // 고객지원에 필요한 회원정보
    boolean register(RegisterDTO registerDTO);   // 회원가입
}
