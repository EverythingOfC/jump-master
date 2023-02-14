package com.example.jump.service;


import com.example.jump.domain.SearchApi;
import com.example.jump.repository.MetaSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MetaSearchImpl implements MetaSearch{   // 검색 서비스

    private final MetaSearchRepository metaRepository;    // 검색 테이블에 접근하기 위한 객체

    public List<SearchApi> searchApi(String title){     // 지정된 title로 검색

        return this.metaRepository.findByTitleContaining(title);   // 검색된 목록들을 반환

        // ""
    }

    public List<SearchApi> findAll(){   // 전체 title 검색
        return this.metaRepository.findAll();
    }
}
