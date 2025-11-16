package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
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
 * Тесты для {@link TransferService}, покрывающие бизнес-логику
 * перевода средств между картами одного и того же пользователя.
 *
 * <p>В тестах используется Mockito для изоляции слоя сервисов от реальной БД.
 * Тестируются основные сценарии:
 * <ul>
 *     <li>Успешный перевод между двумя активными картами пользователя</li>
 *     <li>Ошибка перевода при недостаточном балансе</li>
 * </ul>
 * </p>
 */
@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    /** Мок репозитория карт, используется для подмены запросов к БД. */
    @Mock
    private CardRepository cardRepository;

    /** Мок сервиса пользователей, возвращает тестового пользователя. */
    @Mock
    private UserService userService;

    /** Тестируемый сервис, в который внедряются моки. */
    @InjectMocks
    private TransferService transferService;

    /** Тестовый пользователь. */
    private User testUser;

    /** Карта-отправитель. */
    private Card fromCard;

    /** Карта-получатель. */
    private Card toCard;

    /** Запрос перевода, используемый в тестах. */
    private TransferRequest transferRequest;

    /**
     * Инициализация данных перед каждым тестом.
     * Создаются:
     * <ul>
     *     <li>Пользователь testuser</li>
     *     <li>Две карты с балансами 1000 и 500</li>
     *     <li>Объект TransferRequest на 200 единиц</li>
     * </ul>
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setUser(testUser);
        fromCard.setStatus(Card.CardStatus.ACTIVE);
        fromCard.setBalance(BigDecimal.valueOf(1000));
        fromCard.setExpirationDate(LocalDate.now().plusYears(1));

        toCard = new Card();
        toCard.setId(2L);
        toCard.setUser(testUser);
        toCard.setStatus(Card.CardStatus.ACTIVE);
        toCard.setBalance(BigDecimal.valueOf(500));
        toCard.setExpirationDate(LocalDate.now().plusYears(2));

        transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(BigDecimal.valueOf(200));
    }

    /**
     * Проверяет успешный сценарий перевода между двумя картами пользователя.
     *
     * <p>Ожидаемое поведение:
     * <ul>
     *     <li>Баланс карты-отправителя уменьшается</li>
     *     <li>Баланс карты-получателя увеличивается</li>
     *     <li>Оба обновления сохраняются в репозиторий</li>
     *     <li>Метод НЕ бросает исключений</li>
     * </ul>
     * </p>
     */
    @Test
    void transferBetweenOwnCards_ValidTransfer_CompletesSuccessfully() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndUser(2L, testUser)).thenReturn(Optional.of(toCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> transferService.transferBetweenOwnCards(transferRequest, "testuser"));

        assertEquals(BigDecimal.valueOf(800), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(700), toCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    /**
     * Проверяет обработку ошибки в ситуации,
     * когда баланс карты-отправителя меньше суммы перевода.
     *
     * <p>Ожидаемое поведение:
     * <ul>
     *     <li>Метод выбрасывает RuntimeException с сообщением "Insufficient funds"</li>
     *     <li>Репозиторий <b>не</b> вызывает save()</li>
     *     <li>Баланс карт остаётся неизменным</li>
     * </ul>
     * </p>
     */
    @Test
    void transferBetweenOwnCards_InsufficientFunds_ThrowsException() {
        fromCard.setBalance(BigDecimal.valueOf(100)); // меньше, чем 200
        transferRequest.setAmount(BigDecimal.valueOf(200));

        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndUser(2L, testUser)).thenReturn(Optional.of(toCard));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transferService.transferBetweenOwnCards(transferRequest, "testuser");
        });

        assertEquals("Insufficient funds", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }
}