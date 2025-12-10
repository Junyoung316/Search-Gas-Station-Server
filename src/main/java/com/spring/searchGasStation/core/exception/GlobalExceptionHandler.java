package com.spring.searchGasStation.core.exception;

import com.spring.searchGasStation.core.dto.MainResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MainResponse<Void>> handleAllExceptions(Exception ex) {
        log.error("[서버 오류 로그]: ", ex);
        MainResponse<Void> errorResponse = MainResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "서버 오류: " + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(CustomJwtException.class)
    public ResponseEntity<MainResponse<Void>> handleJwtException(CustomJwtException ex) {
        log.error("JWT 예외 발생: {}", ex.getMessage(), ex.getCause());
        MainResponse<Void> errorResponse = MainResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                "JWT 오류: " + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(CustomNoResourcesJwtException.class)
    public ResponseEntity<MainResponse<Void>> handleNoResourcesException(CustomNoResourcesJwtException ex) {
        log.error("JWT 리소스 예외 발생: {}", ex.getMessage(), ex.getCause());
        MainResponse<Void> errorResponse = MainResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                "JWT 리소스 오류: " + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
}
