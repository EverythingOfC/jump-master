package com.example.jump.repository;

import com.example.jump.domain.ClientSupportApi;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientSupportApiRepository extends JpaRepository<ClientSupportApi, Long> {     // 고객지원 테이블에 접근하기 위한 레퍼지토리

}
