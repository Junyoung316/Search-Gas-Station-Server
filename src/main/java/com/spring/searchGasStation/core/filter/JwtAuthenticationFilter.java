package com.spring.searchGasStation.core.filter;

import com.spring.searchGasStation.core.util.jwt.JwtService;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override // Authentication 인증 필터
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException, java.io.IOException {
        String token = extractToken(request);

        if (token != null) {
            try {
                // 1. JWT 유효성 검사 (예외가 발생하지 않으면 성공)
                jwtService.validateJwtToken(token);

                // 2. JWT에서 email, role 추출
                String email = jwtService.extractEmailFromToken(token);
                String role = jwtService.extractFromToken(token, "role", String.class);

                // 3. Authentication 객체 생성
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        email, null, Collections.singletonList(authority)
                );

                // 4. SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("[JWT 인증 성공] email={}, role={}", email, role);

            } catch (Exception e) {
                log.warn("[JWT 인증 실패] 경로: {}, 예외: {}", request.getRequestURI(), e.getMessage());
                SecurityContextHolder.clearContext(); // 컨텍스트 클리어

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 403
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"status\": 401, \"error\": \"유효하지 않은 토큰입니다.\"}");
                return; // 필터 체인 중단
            }
        } else {
            log.info("[JWT 필터] 토큰 없음: {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    // 토큰 추출 로직
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

}