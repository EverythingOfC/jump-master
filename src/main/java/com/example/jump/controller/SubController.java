package com.example.jump.controller;

import com.example.jump.domain.MetaApi;
import com.example.jump.service.MetaService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor    // final이 붙은 속성을 초기화하는 생성자를 만들어줌.
@Controller
public class SubController {

    private final MetaService metaService;  // 서비스 객체를 생성자 주입


    @GetMapping({"index","/"})    // 메인 페이지
    public String index(Model model, @AuthenticationPrincipal User user) {
        if(user == null){
            model.addAttribute("message","null");
        }
        else{
            model.addAttribute("message",user.getUsername());
        }
        return "index";
    }

    @GetMapping("/jump/delete")      // 삭제
    public String delete(@RequestParam(value="delete",defaultValue = "0") String[] metaId,
                         @RequestParam(value="listPage")int listPage,
                         @RequestParam(value="type")String type
                         ){  // form의 checkbox에 있는 metaId값을 배열로 받아와서 여러 개의 데이터 삭제

        metaService.delete(metaId);

        try {
            type = new String(type.getBytes("utf-8"),"ISO-8859-1"); // 파일명 한글 처리
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return "redirect:/jump/list?listPage=" +listPage+"&type="+type;    // 삭제해도 페이지 값은 유지
    }

    @GetMapping("/jump/detail")     // 상세
    public String detail(Model model, String metaId, int listPage,String type){   // model클래스를 통해 데이터를 템플릿에 전달함.
        MetaApi meta = metaService.getView(metaId);
        model.addAttribute("metaView",meta);

        model.addAttribute("listPage",listPage);
        model.addAttribute("type",type);
        return "detail";
    }


    @GetMapping("/jump/list")   // 전체보기
    public String list(Model model, @RequestParam(value="listPage",defaultValue = "1") int page,
                                    @RequestParam(value="type")String type){

        Page<MetaApi> meta = metaService.getList(page-1,type);   // Page객체는 실제 페이지를 0부터 계산하므로

        // 뷰에서 사용할 속성을 지정한다.
        model.addAttribute("paging",meta);      // Page<MetaApi>객체
        model.addAttribute("listPage",page);    // 현재 페이지 정보
        model.addAttribute("type",type);        // 자료 유형
        model.addAttribute("size",meta.getTotalElements());  // 데이터 개수

        return "list";
    }

}
