package com.example.jump.service;

import com.example.jump.domain.MetaApi;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import java.util.List;


public interface MetaData {      // 메타정보 관련 인터페이스

    Page<MetaApi> getList(int page,String type);    // api 페이징 리스트
    List<MetaApi> findAll(String type); // api 리스트
    MetaApi getView(String id);     // api 상세
    void delete(String[] id);       // api 삭제
    void save(MetaApi meta);        // api 저장
    void getApi(String type);       // 요청에 맞는 api 출력
    void getApiUpdate(String type,String startDate,String endDate); // 요청에 맞는 증분데이터 처리
    ResponseEntity<byte[]> saveCsv(String type);    // csv 저장

}
