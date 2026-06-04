package com.bitcoin.testnet.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Data
    @Builder
    public static class ApiError {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
    }

    @ExceptionHandler(InvalidAddressException.class)
    public ResponseEntity<ApiError> handleInvalidAddress(InvalidAddressException ex, HttpServletRequest req) {
        log.warn("Adresse Bitcoin invalide: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(buildError(400, "Invalid Bitcoin Address", ex.getMessage(), req));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ApiError> handleInsufficientFunds(InsufficientFundsException ex, HttpServletRequest req) {
        log.warn("Fonds insuffisants: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(buildError(400, "Insufficient Funds", ex.getMessage(), req));
    }

    @ExceptionHandler(WalletNotReadyException.class)
    public ResponseEntity<ApiError> handleWalletNotReady(WalletNotReadyException ex, HttpServletRequest req) {
        log.warn("Portefeuille non prêt: {}", ex.getMessage());
        return ResponseEntity.status(503).body(buildError(503, "Wallet Not Ready", ex.getMessage(), req));
    }

    @ExceptionHandler(BitcoinException.class)
    public ResponseEntity<ApiError> handleBitcoinException(BitcoinException ex, HttpServletRequest req) {
        log.error("Erreur Bitcoin: {}", ex.getMessage());
        return ResponseEntity.status(500).body(buildError(500, "Bitcoin Error", ex.getMessage(), req));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(buildError(400, "Validation Error", message, req));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Erreur inattendue: {}", ex.getMessage(), ex);
        return ResponseEntity.status(500).body(buildError(500, "Internal Server Error", ex.getMessage(), req));
    }

    private ApiError buildError(int status, String error, String message, HttpServletRequest req) {
        return ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(req.getRequestURI())
                .build();
    }
}
