package com.example.jump.repository;


import com.example.jump.domain.MetaApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface MetaRepository extends JpaRepository<MetaApi, String> { // 엔티티와 엔티티의 pk를 적어줌.

    List<MetaApi> findAllByMetaTypeContaining(String type);   // 유형으로 데이터를 찾아옴.
    Page<MetaApi> findAllByMetaType(Pageable pageable, String type);      // 유형으로 데이터를 찾아와서 페이징 처리
    Page<MetaApi> findAll(Pageable pageable);           // 전체 페이징 처리


}
