package com.example.bankcards.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO для передачи профиля пользователя.
 *
 * <p>Используется для отображения информации о пользователе в API, например, в списках пользователей или при просмотре профиля.</p>
 */
@Data
public class UserProfile {

    /** Уникальный идентификатор пользователя */
    private Long id;

    /** Имя пользователя (username) */
    private String username;

    /** Электронная почта пользователя */
    private String email;

    /** Имя пользователя */
    private String firstName;

    /** Фамилия пользователя */
    private String lastName;

    /** Дата и время создания пользователя в системе */
    private LocalDateTime createdAt;
}