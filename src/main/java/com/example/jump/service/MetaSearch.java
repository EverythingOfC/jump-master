package com.example.jump.service;

import com.example.jump.domain.SearchApi;
import java.util.List;

public interface MetaSearch {   // API 검색 인터페이스
    List<SearchApi> searchApi(String title);    // 특정 API명 검색
    List<SearchApi> findAll();  // 전체 API 값 출력
}
