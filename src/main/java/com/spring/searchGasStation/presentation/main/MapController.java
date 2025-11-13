package com.spring.searchGasStation.presentation.main;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
        return "map"; // 우선 map 페이지로 연결
    }

}
