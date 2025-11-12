package com.example.bankcards.dto;

import lombok.Data;

/**
 * DTO-объект, представляющий ответ при успешной аутентификации пользователя.
 * <p>
 * Возвращается контроллером {@code AuthController} после успешного входа в систему.
 * Содержит JWT-токен и основную информацию о пользователе.
 */
@Data
public class AuthResponse {

    /**
     * Сгенерированный JWT-токен для дальнейшей аутентификации запросов.
     */
    private String token;

    /**
     * Тип токена. По умолчанию используется "Bearer".
     */
    private String type = "Bearer";

    /**
     * Уникальный идентификатор пользователя.
     */
    private Long id;

    /**
     * Имя пользователя (логин).
     */
    private String username;

    /**
     * Адрес электронной почты пользователя.
     */
    private String email;

    /**
     * Имя пользователя.
     */
    private String firstName;

    /**
     * Фамилия пользователя.
     */
    private String lastName;

    /**
     * Конструктор для создания объекта {@link AuthResponse}.
     *
     * @param token     JWT-токен.
     * @param id        идентификатор пользователя.
     * @param username  логин пользователя.
     * @param email     email пользователя.
     * @param firstName имя пользователя.
     * @param lastName  фамилия пользователя.
     */
    public AuthResponse(String token, Long id, String username, String email, String firstName, String lastName) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}