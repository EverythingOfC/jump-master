package com.example.jump.service;

import com.example.jump.domain.SearchApi;

import java.util.List;

public interface MetaSearch {   // 검색 인터페이스

    List<SearchApi> searchApi(String title);
    List<SearchApi> findAll();
}
