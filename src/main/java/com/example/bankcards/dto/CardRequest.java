package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO для запроса создания или обновления банковской карты.
 *
 * <p>Используется как тело запроса в контроллерах для операций с картами.</p>
 */
@Data
public class CardRequest {

    /** Номер карты, должен быть от 16 до 19 цифр */
    @NotBlank
    @Size(min = 16, max = 19, message = "Card number must be between 16 and 19 digits")
    private String cardNumber;

    /** Имя владельца карты, отображаемое на карте */
    @NotBlank
    private String cardHolderName;

    /** Дата окончания действия карты */
    @NotNull
    private LocalDate expirationDate;

    /** Начальный баланс карты, по умолчанию 0 */
    private BigDecimal balance = BigDecimal.ZERO;

    /** Имя пользователя, которому создается карта (только для администраторов) */
    private String username;
}