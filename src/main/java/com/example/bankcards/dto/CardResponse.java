package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO для ответа с информацией о банковской карте.
 *
 * <p>Используется для передачи данных о карте клиенту через REST API.</p>
 */
@Data
public class CardResponse {

    /** Уникальный идентификатор карты */
    private Long id;

    /** Маскированный номер карты для отображения (например, "**** **** **** 1234") */
    private String cardNumberMasked;

    /** Имя владельца карты */
    private String cardHolderName;

    /** Дата окончания действия карты */
    private LocalDate expirationDate;

    /** Статус карты (ACTIVE, BLOCKED, EXPIRED) */
    private Card.CardStatus status;

    /** Текущий баланс карты */
    private BigDecimal balance;

    /** Дата и время создания записи карты */
    private LocalDateTime createdAt;

    /** Дата и время последнего обновления записи карты */
    private LocalDateTime updatedAt;

    /**
     * Конструктор для создания CardResponse.
     *
     * @param id уникальный идентификатор карты
     * @param cardNumberMasked маскированный номер карты
     * @param cardHolderName имя владельца карты
     * @param expirationDate дата окончания действия карты
     * @param status статус карты
     * @param balance текущий баланс карты
     * @param createdAt дата и время создания карты
     * @param updatedAt дата и время последнего обновления карты
     */
    public CardResponse(Long id, String cardNumberMasked, String cardHolderName,
                        LocalDate expirationDate, Card.CardStatus status,
                        BigDecimal balance, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.cardNumberMasked = cardNumberMasked;
        this.cardHolderName = cardHolderName;
        this.expirationDate = expirationDate;
        this.status = status;
        this.balance = balance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}