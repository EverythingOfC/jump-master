package com.example.jump.controller;


import com.example.jump.domain.MetaApi;
import com.example.jump.service.MetaSearch;
import com.example.jump.service.MetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Controller
public class MainController {   // API, JSON, OAK 처리

    @Autowired
    private MetaData metaService;

    @GetMapping("/jump/api")    // 해당 API 저장 및 출력
    public String api(@RequestParam(value = "type") String type) {

        this.metaService.getApi(type);     // api 출력을 위한 서비스 메소드

        try {
            type = new String(type.getBytes("utf-8"), "ISO-8859-1");     //  쿼리스트링 한글 처리
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return "redirect:/jump/list?type=" + type;  // list.html 호출
    }

    @GetMapping("/jump/json")   // 해당 API를 Json으로 출력
    public @ResponseBody List<MetaApi> json(@RequestParam(value="type") String type){   // 키와 값 구조의 데이터를 JSON형태로 리턴
        List<MetaApi> metaApi = this.metaService.findAll(type); // 해당 형식의 데이터 저장

        return metaApi; // json형식의 데이터 리턴
    }

    @GetMapping("/jump/save")   // csv파일로 저장
    public ResponseEntity<byte[]> saveCsv(@RequestParam(value = "type") String type) {

        return metaService.saveCsv(type);
    }

    @GetMapping("/jump/detail")     // 상세
    public String detail(Model model, String metaId, int listPage,String type){// model클래스를 통해 데이터를 템플릿에 전달함.
        MetaApi meta = metaService.getView(metaId);

        model.addAttribute("metaView",meta);
        model.addAttribute("listPage",listPage);
        model.addAttribute("type",type);
        return "detail";
    }

    @GetMapping("/jump/delete")      // 삭제
    public String delete(@RequestParam(value="delete",defaultValue = "0") String[] metaId, // form의 checkbox에 있는 metaId값을 배열로 받아와서 여러 개의 데이터 삭제
                         @RequestParam(value="listPage")int listPage,
                         @RequestParam(value="type")String type
    ){
        metaService.delete(metaId);
        try {
            type = new String(type.getBytes("utf-8"),"ISO-8859-1"); // 파일명 한글 처리
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return "redirect:/jump/list?listPage=" +listPage+"&type="+type;    // 삭제해도 페이지 값은 유지
    }

    @GetMapping("/jump/list")   // 전체보기
    public String list(Model model, @RequestParam(value="listPage",defaultValue = "1") int page,
                       @RequestParam(value="type")String type){

        // Page객체는 실제 페이지를 0부터 계산하므로
        Page<MetaApi> meta = page>0? metaService.getList(page-1,type) : metaService.getList(1,type);

        // 뷰에서 사용할 속성을 지정한다.
        model.addAttribute("paging",meta);      // Page<MetaApi>객체
        model.addAttribute("listPage",page);    // 현재 페이지 정보
        model.addAttribute("type",type);        // 자료 유형
        model.addAttribute("size",meta.getTotalElements());  // 데이터 개수

        return "list";
    }

}
