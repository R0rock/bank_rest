package com.example.bankcards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Сущность, представляющая банковскую карту пользователя.
 * <p>
 * Содержит основную информацию о карте: номер (в зашифрованном и маскированном виде),
 * владельца, срок действия, баланс, статус и дату создания/обновления.
 * <p>
 * Таблица в базе данных: {@code cards}.
 */
@Entity
@Table(name = "cards")
@Data
public class Card {

    /**
     * Уникальный идентификатор карты (первичный ключ).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Зашифрованный номер карты, хранящийся в базе данных.
     * Используется для обеспечения безопасности.
     */
    @Column(name = "card_number_encrypted")
    private String cardNumberEncrypted;

    /**
     * Маскированный номер карты, предназначенный для отображения пользователю
     * (например, "**** **** **** 1234").
     */
    @Column(name = "card_number_masked")
    private String cardNumberMasked;

    /**
     * Имя держателя карты, как указано на карте.
     */
    @NotBlank
    private String cardHolderName;

    /**
     * Дата окончания срока действия карты (месяц и год).
     */
    @NotNull
    private LocalDate expirationDate;

    /**
     * Текущий статус карты (активна, заблокирована, просрочена).
     */
    @Enumerated(EnumType.STRING)
    private CardStatus status = CardStatus.ACTIVE;

    /**
     * Баланс карты в денежном выражении.
     */
    @NotNull
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Пользователь, которому принадлежит данная карта.
     * Связь многие-к-одному с сущностью {@link User}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Дата и время создания записи в базе данных.
     */
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления записи.
     */
    private LocalDateTime updatedAt;

    /**
     * Автоматически устанавливает дату создания и обновления
     * перед сохранением новой записи в базу данных.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Автоматически обновляет дату изменения
     * перед сохранением изменений в существующей записи.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Перечисление возможных статусов банковской карты.
     */
    public enum CardStatus {
        /**
         * Карта активна и доступна для операций.
         */
        ACTIVE,

        /**
         * Карта временно или навсегда заблокирована.
         */
        BLOCKED,

        /**
         * Срок действия карты истёк.
         */
        EXPIRED
    }
}