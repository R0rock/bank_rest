package com.example.bankcards.exception;

import com.example.bankcards.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений для REST-контроллеров.
 *
 * <p>Перехватывает исключения, возникающие в приложении, и возвращает
 * стандартизированные ответы API с информацией об ошибках.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает ошибки валидации аргументов метода (@Valid).
     *
     * @param ex исключение MethodArgumentNotValidException
     * @return ResponseEntity с ApiResponse, содержащим информацию о полях с ошибками
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(ApiResponse.error("Validation failed", errors));
    }

    /**
     * Обрабатывает общие исключения времени выполнения.
     *
     * @param ex исключение RuntimeException
     * @return ResponseEntity с ApiResponse, содержащим сообщение об ошибке
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Обрабатывает все остальные неперехваченные исключения.
     *
     * @param ex любое исключение типа Exception
     * @return ResponseEntity с ApiResponse и статусом 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error: " + ex.getMessage()));
    }
}