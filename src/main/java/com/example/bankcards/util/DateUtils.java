package com.example.bankcards.util;

import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Утилитарный класс для работы с датами, связанными с банковскими картами.
 *
 * <p>Предоставляет методы для форматирования даты истечения срока карты
 * и проверки, истек ли срок действия карты.</p>
 */
@Component
public class DateUtils {

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");

    /**
     * Форматирует дату истечения срока карты в виде "MM/yy".
     *
     * @param date дата истечения срока карты
     * @return строка с отформатированной датой, например "12/25"
     */
    public String formatExpirationDate(LocalDate date) {
        return date.format(DISPLAY_FORMATTER);
    }

    /**
     * Проверяет, истек ли срок действия карты.
     *
     * @param expirationDate дата истечения срока карты
     * @return {@code true}, если карта просрочена, {@code false} в противном случае
     */
    public boolean isCardExpired(LocalDate expirationDate) {
        return expirationDate.isBefore(LocalDate.now());
    }
}