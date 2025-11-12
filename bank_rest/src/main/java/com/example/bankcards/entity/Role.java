package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Сущность, представляющая роль пользователя в системе.
 * <p>
 * Роли определяют уровень доступа и права пользователя
 * при работе с приложением (например, обычный пользователь или администратор).
 * <p>
 * Таблица в базе данных: {@code roles}.
 */
@Entity
@Table(name = "roles")
@Data
public class Role {

    /**
     * Уникальный идентификатор роли (первичный ключ).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя роли, определяющее её тип.
     * <p>
     * Значения перечислены в {@link RoleName}.
     * Каждая роль уникальна и не может дублироваться в базе данных.
     */
    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private RoleName name;

    /**
     * Перечисление возможных ролей в системе.
     * <ul>
     *     <li>{@link #ROLE_USER} — базовая роль для обычных пользователей.</li>
     *     <li>{@link #ROLE_ADMIN} — роль администратора с расширенными правами доступа.</li>
     * </ul>
     */
    public enum RoleName {
        /**
         * Роль обычного пользователя системы.
         */
        ROLE_USER,

        /**
         * Роль администратора системы.
         */
        ROLE_ADMIN
    }
}