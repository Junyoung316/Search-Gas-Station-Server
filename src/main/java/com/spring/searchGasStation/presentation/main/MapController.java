package com.spring.searchGasStation.presentation.main;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
public class MapController {

    @Value("${kakao.maps.script-key}")
    private String kakaoApiKey;

    @GetMapping("/map")
    public String mapPage(Model model) {
        // resources/templates/map.html 파일을 찾아서 렌더링합니다.
        model.addAttribute("kakaoApiKey", kakaoApiKey);
        return "map";
    }

    // (루트 페이지도 추가)
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("kakaoApiKey", kakaoApiKey);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            System.out.println("============= 🔎 로그인 상태 디버깅 =============");
            System.out.println("1. 이름(username): " + auth.getName()); // sec:authentication="name"
            System.out.println("2. 권한(Role): " + auth.getAuthorities()); // sec:authentication="principal.authorities"
            System.out.println("3. 로그인 여부: " + auth.isAuthenticated()); // sec:authorize="isAuthenticated()"
            System.out.println("4. 사용자 타입: " + auth.getPrincipal().getClass().getName()); // UserDetails인지, String(anonymous)인지 확인
            System.out.println("=============================================");
        }

        return "map"; // 우선 map 페이지로 연결
    }

}
