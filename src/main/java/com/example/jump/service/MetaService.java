package com.example.jump.service;

import com.example.jump.domain.ClientSupportApi;
import com.example.jump.domain.MetaApi;
import com.example.jump.domain.SearchApi;
import com.example.jump.entity.ClubMember;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;


public interface MetaService {  // 서비스 인터페이스

    Page<MetaApi> getList(int page,String type);    // 모든 api 리스트 반환
    MetaApi getView(String id);     // api 상세
    void delete(String[] id);       // api 삭제
    void save(MetaApi meta);        // api 저장
    void getApi(String type);       // 요청에 맞는 api 출력

    ResponseEntity<byte[]> saveCsv(String type);
    List<SearchApi> searchApi(String title);    // 검색
    ClientSupportApi supportSave(String category, String title, String name, String content, String method);     // 고객지원 저장

    List<ClientSupportApi> boardlist();

    ClientSupportApi boardview(Long CId);

    ClubMember getUser(String email);
}
