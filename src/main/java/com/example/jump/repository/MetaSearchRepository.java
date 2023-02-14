package com.example.jump.repository;

import com.example.jump.domain.SearchApi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetaSearchRepository extends JpaRepository<SearchApi,Long> {     // 엔티티 클래스명과 엔티티의 pk를 적어줌.

    List<SearchApi> findByTitleContaining(String title);   // 제목으로 api 검색


}
