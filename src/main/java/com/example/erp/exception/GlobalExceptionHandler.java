package com.example.erp.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import java.util.Map;
import java.util.stream.Collectors;

// Deliberately no catch-all Exception handler — every thrown exception must be
// an AppException (or one of the other handled types below) so its response
// body matches this envelope; a ResponseStatusException instead falls through
// to Spring's default handling, which dispatches to /error and returns the raw
// {timestamp, status, error, path} whitelabel body instead of this shape.
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    ResponseEntity<Map<String, Object>> handleAppException(AppException ex) {
        log.warn("status={} message={}", ex.getStatus().value(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(Map.of("statusCode", ex.getStatus().value(), "message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        log.warn("status=400 message=Validation failed errors={}", errors);
        return ResponseEntity.badRequest()
                .body(Map.of("statusCode", 400, "message", "Validation failed", "errors", errors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("statusCode", 403, "message", "Access denied"));
    }

    // Covers MaxUploadSizeExceededException too (it extends MultipartException) —
    // without this, an oversized file falls through to Spring's whitelabel /error
    // handling instead of this envelope, same reasoning as the no-catch-all note above.
    @ExceptionHandler(MultipartException.class)
    ResponseEntity<Map<String, Object>> handleMultipart(MultipartException ex) {
        log.warn("status=400 message=Multipart request error: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of("statusCode", 400, "message", "Invalid file upload — check the file size and try again."));
    }
}
