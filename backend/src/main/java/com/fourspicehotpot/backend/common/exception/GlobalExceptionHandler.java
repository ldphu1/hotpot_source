package com.fourspicehotpot.backend.common.exception;

import com.fourspicehotpot.backend.modules.thucdon.dto.response.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
                new ApiResponse(false, ex.getMessage(), null)
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDB(DataIntegrityViolationException ex) {
        return ResponseEntity.badRequest().body(
                new ApiResponse(false, "Dữ liệu đã tồn tại", null)
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        return ResponseEntity.badRequest().body(
                new ApiResponse(false, message, null)
        );
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> handleForbidden(SecurityException ex) {
        return ResponseEntity.status(403).body(
                new ApiResponse(false, "Bạn không có quyền truy cập", null)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {

        ex.printStackTrace(); // in lỗi thật ra console

        return ResponseEntity.status(500).body(
                new ApiResponse(false, ex.getMessage(), null)
        );
    }
}