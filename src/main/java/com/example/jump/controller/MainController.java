package com.example.jump.controller;

import com.example.jump.domain.ClientSupportApi;
import com.example.jump.domain.MetaApi;
import com.example.jump.domain.RegisterDTO;
import com.example.jump.domain.SearchApi;
import com.example.jump.entity.ClubMember;
import com.example.jump.service.MetaSearch;
import com.example.jump.service.MetaService;
import com.example.jump.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Controller
public class MainController {   // api출력 및 저장하는 핵심 로직

    @Autowired  // 자동으로 의존 객체를 찾아서 주입함
    private MetaService metaService;

    @Autowired  // 자동으로 의존 객체를 찾아서 주입함
    private MetaSearch metaSearch;


    @Autowired
    private RegisterService registerService;

    // jump/api?type=보도자료

    @GetMapping("/jump/api")    // 해당 API저장 및 출력
    public String api(@RequestParam(value = "type") String type) {

        metaService.getApi(type);     // api출력을 위한 서비스 메소드 실행

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

        return metaApi; // json형식으로 저장된 데이터 리턴
    }

    @GetMapping("/jump/save")   // csv파일로 저장
    public ResponseEntity<byte[]> saveCsv(@RequestParam(value = "type") String type) {

        return metaService.saveCsv(type);
    }

    @GetMapping("/jump/search") // 검색 시 이동
    public String search(@RequestParam(value = "search_kw", defaultValue = "") String search, Model model) {

        List<SearchApi> searchApis = this.metaSearch.searchApi(search);

        model.addAttribute("search", searchApis);    // 검색 결과를 모델의 속성으로 저장
        model.addAttribute("kw",search);

        return "api";   // 자동으로 resources/templates/api;
    }

    @GetMapping("/login")   // 로그인
    public String login() {
        return "login";
    }

    @GetMapping("/register")    // 등록 폼(Get)
    public String register() {
        return "register";
    }

    @PostMapping("/register")   // 등록 폼(Post)
    public String postRegister(RegisterDTO registerDTO, Model model) {
        if (registerService.register(registerDTO)) {
            return "redirect:/";
        } else {
            model.addAttribute("error", "--중복된 이메일--");
            return "/register";
        }
    }

    @GetMapping("/jump/support")        // 고객지원 폼
    public String support(Model model, @AuthenticationPrincipal User user) {

        ClubMember clubMember = metaService.getUser(user.getUsername()); // 로그인된 회원 이메일을 넘김
        List<SearchApi> search = metaSearch.findAll();  // 전체 API명 조회

        if (clubMember == null) {
            model.addAttribute("user", clubMember);
        } else {
            model.addAttribute("user", clubMember);
        }

        model.addAttribute("search",search);

        return "support";


    }


    @GetMapping("/jump/supportHandle")  // 고객지원 처리
    public String supportHandle(@RequestParam(value = "sup_category") String category,
                                @RequestParam(value = "sup_title") String title,
                                @RequestParam(value = "sup_name", defaultValue = "") String name,
                                @RequestParam(value = "sup_content") String content,
                                @RequestParam(value = "sup_email") String email, Model model) {

        ClientSupportApi api = this.metaService.supportSave(category, title, name, content, email);    // 고객지원 데이터 저장

        return "redirect:/jump/search";  // api목록으로 이동
    }

    @GetMapping("/jump/supportList")
    public String boardList(Model model) {
        model.addAttribute("list", this.metaService.boardlist());
        return "boardlist";
    }

    @GetMapping("/jump/supportview")
    public String boardView(Model model, Long CId) {
        model.addAttribute("view", this.metaService.boardview(CId));
        return "boardview";
    }
}
