package com.example.jump.domain;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class MetaDTO {  // DTO 객체 ( 컨트롤러 <--> 서비스 )

    private Long metaId;    // ID
    private String metaClassifications; // 분야
    private String metaType;        // 유형
    private String metaTitle;       // 제목
    private String metaSubjects;    // 주제어
    private String metaDescription; // 설명
    private String metaPublisher;   // 발행기관(없다면 출처(연계시스템) 등록)
    private String metaContributor; // 원작자
    private LocalDateTime metaDate; // 날짜
    private String metaLanguage;    // 언어
    private String metaIdentifier;  // 식별자
    private String metaFormat;      // 형식
    private String metaRelation;    // 관련자원
    private String metaCoverage;    // 내용범위
    private String metaRight;       // 이용조건
}
