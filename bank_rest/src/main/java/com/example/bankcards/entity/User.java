package com.example.bankcards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Сущность, представляющая пользователя системы.
 * <p>
 * Содержит данные для аутентификации и базовую информацию о пользователе.
 * Также хранит набор ролей, определяющих уровень доступа ({@link Role}).
 * <p>
 * Таблица в базе данных: {@code users}.
 */
@Entity
@Table(name = "users")
@Data
public class User {

    /**
     * Уникальный идентификатор пользователя (первичный ключ).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Уникальное имя пользователя (логин).
     */
    @NotBlank
    @Column(unique = true)
    private String username;

    /**
     * Зашифрованный пароль пользователя.
     */
    @NotBlank
    private String password;

    /**
     * Уникальный адрес электронной почты пользователя.
     */
    @Email
    @Column(unique = true)
    private String email;

    /**
     * Имя пользователя.
     */
    @NotBlank
    private String firstName;

    /**
     * Фамилия пользователя.
     */
    @NotBlank
    private String lastName;

    /**
     * Набор ролей, назначенных пользователю.
     * <p>
     * Определяет права доступа в системе.
     * Используется связь "многие-ко-многим" с таблицей {@code roles}.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * Флаг активности учётной записи.
     * <p>
     * {@code true} — пользователь активен и может входить в систему.
     * {@code false} — учётная запись заблокирована или деактивирована.
     */
    private boolean enabled = true;

    /**
     * Дата и время создания записи пользователя в базе данных.
     */
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления данных пользователя.
     */
    private LocalDateTime updatedAt;

    /**
     * Автоматически устанавливает дату создания и обновления
     * перед добавлением новой записи в базу данных.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Автоматически обновляет дату изменения
     * перед сохранением существующей записи.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}