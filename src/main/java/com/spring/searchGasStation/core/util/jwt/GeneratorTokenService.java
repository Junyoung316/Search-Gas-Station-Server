package com.spring.searchGasStation.core.util.jwt;

import com.spring.searchGasStation.core.dto.TokenIatExp;
import com.spring.searchGasStation.core.util.DateTimeUtils;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class GeneratorTokenService {

    @Value("${jwt.access-token-expire-ms}")
    private int accessTokenExpireMs;

    @Value("${jwt.refresh-token-expire-ms}")
    private long refreshTokenExpireMs;

    public String generatorAccessToken(String email, Map<String, Object> claims, SecretKey key) { // Access Token 생성
        Instant now = DateTimeUtils.now();
        Instant expiration = now.plusMillis(accessTokenExpireMs);
        Date issuedAt = DateTimeUtils.instantToDate(now);
        Date expiresAt = DateTimeUtils.instantToDate(expiration);

        String token = Jwts.builder()
                .subject(email)
                .claims(claims)
                .claim("tokenType", "access")
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(key)
                .compact();
        long accessTokenExpireMinutes = accessTokenExpireMs / 1000 / 60;
        log.info("[JWT] Access 토큰 생성: email={}, 생성={}, role={}, 만료={}, {}ms ({}분)", email, DateTimeUtils.krZonedDateTime(now), claims.get("role"), DateTimeUtils.krZonedDateTime(expiration), accessTokenExpireMs, accessTokenExpireMinutes);
        return token;
    }

    public TokenIatExp generatorRefreshToken(String email, SecretKey key) {// Refesh Token 생성
        Instant now = DateTimeUtils.now();
        Instant expiration = now.plusMillis(refreshTokenExpireMs);
        Date issuedAt = DateTimeUtils.instantToDate(now);
        Date expiresAt = DateTimeUtils.instantToDate(expiration);

        String token = Jwts.builder()
                .subject(email)
                .claim("tokenType", "refresh")
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(key)
                .compact();
        long refreshTokenExpireMinutes = refreshTokenExpireMs / 1000 / 60;
        long refreshTokenExpireDay = refreshTokenExpireMinutes / 60 / 24;
        log.info("[JWT] Refresh 토큰 생성: email={}, 생성={}, 만료={}, {}ms ({}분,  {}일)", email, DateTimeUtils.krZonedDateTime(now), DateTimeUtils.krZonedDateTime(expiration), refreshTokenExpireMs, refreshTokenExpireMinutes, refreshTokenExpireDay);
        return new TokenIatExp(token, now, expiration);
    }

}
