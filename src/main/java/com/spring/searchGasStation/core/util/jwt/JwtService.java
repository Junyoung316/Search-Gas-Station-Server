package com.spring.searchGasStation.core.util.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.spring.searchGasStation.core.dto.TokenIatExp;
import com.spring.searchGasStation.core.dto.TokenResponse;
import com.spring.searchGasStation.core.exception.CustomJwtException;
import com.spring.searchGasStation.domain.entity.Member;
import com.spring.searchGasStation.domain.repository.MemberRepository;
import com.spring.searchGasStation.core.util.DateTimeUtils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;

@Slf4j
@Service
//@RequiredArgsConstructor
public class JwtService {

    private final SecretKey key;
    private final MemberRepository memberRepository;
    private final GeneratorTokenService generatorTokenService;

    public JwtService(
            @Value("${jwt.secret-key}") String secretKey,
            MemberRepository memberRepository,
            GeneratorTokenService generatorTokenService
    ) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.memberRepository = memberRepository;
        this.generatorTokenService = generatorTokenService;
    }

    @Transactional
    public TokenResponse generateTokens(String email) { // Access Token, Refresh Token 통합 생성
        Member account = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        String accessToken = generatorTokenService.generatorAccessToken(
                email, Map.of("role", account.getRole()), key
        );
        TokenIatExp refreshTokenInfo = generatorTokenService.generatorRefreshToken(email, key);
        Long expiresAt = DateTimeUtils.instantToEpochSeconds(refreshTokenInfo.expiresAt());
        log.info("[재발급] JWT 토큰 발급 완료: email={}, role={}", email, account.getRole());
        log.info("[JWT 토큰] 생성={}, 만료={}L", DateTimeUtils.krZonedDateTime(refreshTokenInfo.issuedAt()), expiresAt);

        return new TokenResponse(accessToken, refreshTokenInfo.token());
    }

    public String extractEmailFromToken(String token) { // Access 토큰에서 email 추출
        try {
            String email = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            log.info("[JWT] 토큰 파싱: email={} from token", email);
            return email;
        } catch (ExpiredJwtException e) {
            throw new CustomJwtException("만료된 JWT 토큰입니다.", e);
        } catch (SignatureException e) {
            throw new CustomJwtException("JWT 서명 검증에 실패했습니다.", e);
        } catch (MalformedJwtException e) {
            throw new CustomJwtException("잘못된 형식의 JWT 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            throw new CustomJwtException("지원하지 않는 JWT 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            throw new CustomJwtException("JWT 토큰이 null이거나 비어 있습니다.", e);
        } catch (JwtException e) {
            throw new CustomJwtException("JWT 처리 중 알 수 없는 예외가 발생했습니다.", e);
        }
    }

    public <T> T extractFromToken(String token, String claim, Class<T> requiredType) { // 토큰에서 필요한 claim 추출
        try {
            Object value = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get(claim);

            if (value == null) {
                return null;
            }

            if (requiredType == String.class) {
                return requiredType.cast(value.toString());
            } else if (requiredType == Long.class) {
                if (value instanceof Number) {
                    return requiredType.cast(((Number) value).longValue());
                } else if (value instanceof String) {
                    return requiredType.cast(Long.valueOf((String) value));
                }
            } else if (requiredType.isInstance(value)) {
                return requiredType.cast(value);
            }

            throw new IllegalArgumentException("지원하지 않는 클레임 타입 변환입니다.");

        } catch (ExpiredJwtException e) {
            throw new CustomJwtException("만료된 JWT 토큰입니다.", e);
        } catch (SignatureException e) {
            throw new CustomJwtException("JWT 서명 검증에 실패했습니다.", e);
        } catch (MalformedJwtException e) {
            throw new CustomJwtException("잘못된 형식의 JWT 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            throw new CustomJwtException("지원하지 않는 JWT 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            throw new CustomJwtException("JWT 토큰이 null이거나 비어 있습니다.", e);
        } catch (JwtException e) {
            throw new CustomJwtException("JWT 처리 중 알 수 없는 예외가 발생했습니다.", e);
        }
    }

    public boolean isTokenBlacklisted(String token) { // 토큰 블랙리스트 확인
        try {
            // TODO: Redis에서 블랙리스트 확인
//            String blacklistStatus = redisService.getData("blacklist", token);
//            return "logout".equals(blacklistStatus);
            return true;
        } catch (Exception e) {
            log.error("[JWT] 블랙리스트 확인 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateJwtToken(String token) { // 토큰 유효성 검사(서명 및 만료 등)
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            log.info("[JWT] 토큰 검증 성공: token={}", token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new CustomJwtException("만료된 JWT 토큰입니다.", e);
        } catch (SignatureException e) {
            throw new CustomJwtException("JWT 서명 검증에 실패했습니다.", e);
        } catch (MalformedJwtException e) {
            throw new CustomJwtException("잘못된 형식의 JWT 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            throw new CustomJwtException("지원하지 않는 JWT 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            throw new CustomJwtException("JWT 토큰이 null이거나 비어 있습니다.", e);
        } catch (JwtException e) {
            throw new CustomJwtException("JWT 처리 중 알 수 없는 예외가 발생했습니다.", e);
        }
    }
}
