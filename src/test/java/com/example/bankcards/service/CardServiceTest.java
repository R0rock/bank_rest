package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardNumberUtil;
import com.example.bankcards.util.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для {@link CardService}.
 * Проверяют создание карты, шифрование номера, маскирование, корректную привязку к пользователю,
 * а также блокировку карты.
 */

/**
 * Тесты сервиса {@link CardService}, покрывающие:
 * <ul>
 *     <li>Создание карты администратором</li>
 *     <li>Шифрование и маскирование номера карты</li>
 *     <li>Корректность привязки карты к пользователю</li>
 *     <li>Блокировку карты</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserService userService;

    @Mock
    private CardNumberUtil cardNumberUtil;

    @Mock
    private DateUtils dateUtils;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private CardRequest cardRequest;

    /**
     * Инициализация тестовых данных перед каждым тестом.
     * Создаёт:
     * <ul>
     *     <li>Пользователя</li>
     *     <li>Запрос на создание карты</li>
     * </ul>
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        cardRequest = new CardRequest();
        cardRequest.setCardNumber("1234567812345670");
        cardRequest.setCardHolderName("Test User");
        cardRequest.setExpirationDate(LocalDate.now().plusYears(2));
        cardRequest.setBalance(BigDecimal.valueOf(1000));
        cardRequest.setUsername("testuser");
    }

    /**
     * Проверяет корректность создания карты:
     * <ul>
     *     <li>Поиск пользователя</li>
     *     <li>Шифрование номера</li>
     *     <li>Маскирование номера</li>
     *     <li>Сохранение в репозитории</li>
     * </ul>
     */
    @Test
    void createCard_ValidCard_ReturnsCard() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(cardNumberUtil.encrypt(anyString())).thenReturn("encrypted");
        when(cardNumberUtil.maskCardNumber(anyString())).thenReturn("**** **** **** 3456");
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Card result = cardService.createCard(cardRequest);

        assertNotNull(result);
        assertEquals("encrypted", result.getCardNumberEncrypted());
        assertEquals("**** **** **** 3456", result.getCardNumberMasked());
        assertEquals(testUser, result.getUser());
    }

    /**
     * Проверяет корректную блокировку карты.
     * Убедится, что:
     * <ul>
     *     <li>Карта найдена у владельца</li>
     *     <li>Статус изменён на BLOCKED</li>
     *     <li>Изменения сохранены в репозитории</li>
     * </ul>
     */
    @Test
    void blockCard_ValidCard_BlocksCard() {
        Card card = new Card();
        card.setId(1L);
        card.setUser(testUser);
        card.setStatus(Card.CardStatus.ACTIVE);

        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        Card result = cardService.blockCard(1L, "testuser");

        assertEquals(Card.CardStatus.BLOCKED, result.getStatus());
    }
}