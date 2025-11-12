package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO-класс для передачи данных при аутентификации пользователя.
 *
 * <p>Используется при входе в систему для передачи логина и пароля пользователя.
 * Оба поля обязательны для заполнения и проверяются аннотацией {@link NotBlank}.</p>
 *
 * <p>Пример JSON-запроса:</p>
 * <pre>
 * {
 *   "username": "user123",
 *   "password": "mypassword"
 * }
 * </pre>
 */
@Data
public class AuthRequest {

    /**
     * Имя пользователя, используемое для входа в систему.
     */
    @NotBlank
    private String username;

    /**
     * Пароль пользователя для аутентификации.
     */
    @NotBlank
    private String password;
}