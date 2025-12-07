package com.spring.searchGasStation.core.config;

import com.spring.searchGasStation.core.filter.CustomLoggingFilter;
import com.spring.searchGasStation.core.filter.JwtAuthenticationFilter;
import com.spring.searchGasStation.core.util.jwt.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableAspectJAutoProxy
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;

    @Bean
    public PasswordEncoder passwordEncoder() {
//        return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey secretKey(@Value("${jwt.secret-key}") String base64Key) {
        if (base64Key == null || base64Key.isEmpty()) {
            throw new IllegalArgumentException("app.secret-key가 설정되지 않았습니다.");
        }
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        // 디코딩된 바이트로부터 SecretKey 객체(AES용) 생성
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() { // 권한 없는 사용자 제한
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401: 미인증 사용자 요청, 프론트엔드에서 로그인 페이지로 이동 처리
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\": 401, \"error\": \"인증이 필요합니다.\"}");
        };
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .logout(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new CustomLoggingFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(
                    auth -> auth
                            .requestMatchers("/images/**", "/favicon.ico").permitAll()
                            .requestMatchers(
                                    "/",
                                    "/api/**",
                                    "/error"
                            ).permitAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint())
                );
        return http.build();
    }

}
