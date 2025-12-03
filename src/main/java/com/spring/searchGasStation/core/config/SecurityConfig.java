package com.spring.searchGasStation.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableAspectJAutoProxy
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
//        return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                    auth -> auth
                            .requestMatchers("/login", "/signup", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                            .requestMatchers(
                                    "/",
                                    "/map",
                                    "/api/gas-stations",
                                    "/api/station-detail",
                                    "/api/search-stations"
                            ).permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/") // 로그인 페이지가 따로 없으므로 메인으로 (혹은 로그인 모달을 띄울 경로)
                        .loginProcessingUrl("/login-proc") // ★ HTML form의 action과 일치해야 함
                        .usernameParameter("email") // ★ HTML input의 name="email"과 일치
                        .passwordParameter("password") // ★ HTML input의 name="password"와 일치
//                        .defaultSuccessUrl("/?loginSuccess=true", true)
                        .successHandler((request, response, authentication) -> {
                            // 로그인 성공 시 무조건 이 주소로 리다이렉트
                            response.sendRedirect("/?loginSuccess=true");
                        })
                        .failureUrl("/?error=true")   // 실패 시 이동
                        .failureHandler((request, response, exception) -> {
                            System.out.println("🔥🔥🔥 [로그인 실패 원인] 🔥🔥🔥");
                            System.out.println("에러 클래스: " + exception.getClass().getName());
                            System.out.println("에러 메시지: " + exception.getMessage());

                            // 로그인 실패 후 다시 메인으로 이동 (URL에 error 파라미터 붙임)
                            response.sendRedirect("/?error=true");
                        })
                        .permitAll()
                )
                .sessionManagement(session -> session
                        // 1. 세션이 없으면 무조건 만드세요 (IF_REQUIRED)
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)

                        // 2. 로그인 시 세션 ID 변경 (보안 + 로그인 풀림 방지 테스트용)
                        // 안 되면 .none()으로 바꿔보세요.
                        .sessionFixation().changeSessionId()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login") // 로그아웃 후 이동할 곳
                        .invalidateHttpSession(true) // 세션 날리기
                );;

        return http.build();
    }
}
