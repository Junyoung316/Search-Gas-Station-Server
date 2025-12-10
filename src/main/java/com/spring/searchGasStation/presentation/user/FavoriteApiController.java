package com.spring.searchGasStation.presentation.user;

import com.spring.searchGasStation.application.user.favorite.FavoriteService;
import com.spring.searchGasStation.core.dto.MainResponse;
import com.spring.searchGasStation.dto.user.request.FavoriteRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class FavoriteApiController {

    private final FavoriteService favoriteService;

    @GetMapping("/api/favorites/gas-station")
    public ResponseEntity<MainResponse<List<String>>> getFavorites() {
        log.info("getFavorites");
        return ResponseEntity.ok(MainResponse.success(favoriteService.getFavorite()));
    }

    @PostMapping("/api/favorites/gas-station")
    public ResponseEntity<MainResponse<String>> toggleFavorite(
            @RequestBody FavoriteRequestDto requestDto
    ) {
        boolean isFavorited = favoriteService.toggleFavorite(requestDto);
        if (isFavorited) {
            return ResponseEntity.ok(MainResponse.success("단골 주유소로 등록되었습니다."));
        } else {
            return  ResponseEntity.status(210).body(MainResponse.error(210, "단골 주유소에서 해제되었습니다."));
        }
    }
}