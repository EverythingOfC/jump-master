package com.example.jump.controller;

import com.example.jump.domain.ClientSupportApi;
import com.example.jump.domain.RegisterDTO;
import com.example.jump.domain.SearchApi;
import com.example.jump.entity.ClubMember;
import com.example.jump.security.dto.ClubAuthMemberDTO;
import com.example.jump.service.MetaMember;
import com.example.jump.service.MetaSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@RequiredArgsConstructor  // final이 붙은 필드 초기화
@Controller
public class SubController {    // 회원 정보, API 검색

    @Autowired  // 자동으로 의존 객체를 찾아서 주입함
    private MetaSearch metaSearch;
    @Autowired
    private MetaMember metaMember;

    @GetMapping({"index","/"})    // 시작 페이지
    public String index(Model model, @AuthenticationPrincipal ClubAuthMemberDTO user) { // 회원의 이름이 뜨게 함.

        if(user != null)
            model.addAttribute("member",user.getName());

        return "index";
    }

    @GetMapping("/jump/search")     // 검색 시 이동
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
        if (metaMember.register(registerDTO)) {
            return "redirect:/";
        } else {
            model.addAttribute("error", "--중복된 이메일--");
            return "/register";
        }
    }

    @GetMapping("/jump/supportHandle")  // 고객지원 처리
    public String supportHandle(@RequestParam(value = "sup_category") String category,
                                @RequestParam(value = "sup_title") String title,
                                @RequestParam(value = "sup_name", defaultValue = "") String name,
                                @RequestParam(value = "sup_content") String content,
                                @RequestParam(value = "sup_email") String email, Model model) {

        ClientSupportApi api = this.metaMember.supportSave(category, title, name, content, email);    // 고객지원 데이터 저장

        return "redirect:/jump/search";  // api목록으로 이동
    }

    @GetMapping("/jump/support")        // 고객지원 폼
    public String support(Model model, @AuthenticationPrincipal User user) {

        ClubMember clubMember = metaMember.getUser(user.getUsername()); // 로그인된 회원 이메일을 넘김
        List<SearchApi> search = metaSearch.findAll();  // 전체 API명 조회

        if (clubMember == null) {
            model.addAttribute("user", clubMember);
        } else {
            model.addAttribute("user", clubMember);
        }

        model.addAttribute("search",search);

        return "support";
    }


}
