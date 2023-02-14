package com.example.jump.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor  // 매개변수가 없는 생성자
@Entity
@Getter
public class ClientSupportApi {    // 고객지원 테이블

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 고객 ID
    private Long CId;

    @Column(columnDefinition = "TEXT")
    private String cCategory;    // API 카테고리
    @Column(columnDefinition = "TEXT")
    private String cTitle;       // 글 제목
    @Column(columnDefinition = "TEXT")
    private String cName;        // 작성자
    @Column(columnDefinition = "TEXT")
    private String cContent;     // 내용
    @Column(columnDefinition = "TEXT")
    private String cEmail;       // 이메일

    public ClientSupportApi(String cCategory, String cTitle, String cName, String cContent, String cEmail) {
        this.cCategory = cCategory;
        this.cTitle = cTitle;
        this.cName = cName;
        this.cContent = cContent;
        this.cEmail = cEmail;
    }
}
