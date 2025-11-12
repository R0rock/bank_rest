package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO-класс для передачи данных при выполнении перевода между картами.
 *
 * <p>Используется в {@link com.example.bankcards.controller.TransferController}
 * для обработки запросов перевода средств между картами пользователя.</p>
 *
 * <p>Все обязательные поля аннотированы {@link NotNull}, а сумма перевода должна быть положительной,
 * что контролируется аннотацией {@link Positive}.</p>
 *
 * <p>Пример JSON-запроса:</p>
 * <pre>
 * {
 *   "fromCardId": 1,
 *   "toCardId": 2,
 *   "amount": 1500.00,
 *   "description": "Перевод на накопительную карту"
 * }
 * </pre>
 */
@Data
public class TransferRequest {

    /**
     * ID карты, с которой осуществляется перевод.
     * Не может быть {@code null}.
     */
    @NotNull
    private Long fromCardId;

    /**
     * ID карты, на которую осуществляется перевод.
     * Не может быть {@code null}.
     */
    @NotNull
    private Long toCardId;

    /**
     * Сумма перевода. Должна быть положительным числом.
     */
    @NotNull
    @Positive(message = "Transfer amount must be positive")
    private BigDecimal amount;

    /**
     * Необязательное описание перевода (например, «перевод на накопительную карту»).
     */
    private String description;
}