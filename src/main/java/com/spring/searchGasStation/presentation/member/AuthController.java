package com.spring.searchGasStation.presentation.member;

import com.spring.searchGasStation.application.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

//    @GetMapping("/login")
//    public String login() {
//        return "login";
//    }

    private final MemberService memberService;

    // 회원가입 처리
    @PostMapping("/signup")
    public String signup(String email, String password, String nickname) {
        try {
            memberService.join(email, password, nickname);
            return "redirect:/?signupSuccess=true"; // 성공 시 메인으로 이동
        } catch (Exception e) {
            return "redirect:/?error=" + e.getMessage(); // 실패 시 에러 메시지 전달
        }
    }

}
