package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тестовый класс для проверки работы {@link com.example.bankcards.controller.TransferController}.
 *
 * <p>Использует {@link MockMvc} для эмуляции HTTP-запросов к REST API, связанных
 * с переводами между картами одного пользователя.</p>
 *
 * <p>Основные сценарии тестирования:
 * <ul>
 *     <li>Успешный перевод между своими картами</li>
 *     <li>Попытка перевода с недостаточным балансом</li>
 * </ul>
 * </p>
 *
 * <p>Тесты выполняются в транзакции, которая автоматически откатывается после каждого теста
 * благодаря аннотации {@link Transactional}.</p>
 *
 * <p>Активируется профиль {@code test}, что позволяет использовать тестовую БД и отдельные настройки.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class TransferControllerTest {

    /** MockMvc для имитации HTTP-запросов к REST API без запуска реального сервера. */
    @Autowired
    private MockMvc mockMvc;

    /** ObjectMapper используется для сериализации/десериализации JSON в тестах. */
    @Autowired
    private ObjectMapper objectMapper;

    /** Сервис карт, используется для создания тестовых карт и проверки логики перевода. */
    @Autowired
    private CardService cardService;

    /** JWT-токен авторизованного тестового пользователя. */
    private String userToken;

    /** Карта-отправитель для тестового пользователя. */
    private Card fromCard;

    /** Карта-получатель для тестового пользователя. */
    private Card toCard;

    /**
     * Инициализация перед каждым тестом.
     * <p>
     * Создается тестовый пользователь, выполняется регистрация и логин для получения JWT-токена.
     * Также создаются две карты для перевода.
     * </p>
     *
     * @throws Exception если происходит ошибка при выполнении HTTP-запроса
     */
    @BeforeEach
    void setUp() throws Exception {
        AuthRequest registerRequest = new AuthRequest();
        registerRequest.setUsername("transferuser");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        userToken = "Bearer " + objectMapper.readTree(responseBody).path("data").path("token").asText();

        CardRequest cardRequest1 = new CardRequest();
        cardRequest1.setCardNumber("1111222233334444");
        cardRequest1.setCardHolderName("Transfer User");
        cardRequest1.setExpirationDate(LocalDate.now().plusYears(2));
        cardRequest1.setBalance(BigDecimal.valueOf(1000));
        fromCard = cardService.createCardForUser(cardRequest1, "transferuser");

        CardRequest cardRequest2 = new CardRequest();
        cardRequest2.setCardNumber("5555666677778888");
        cardRequest2.setCardHolderName("Transfer User");
        cardRequest2.setExpirationDate(LocalDate.now().plusYears(3));
        cardRequest2.setBalance(BigDecimal.valueOf(500));
        toCard = cardService.createCardForUser(cardRequest2, "transferuser");
    }

    /**
     * Тестирует успешный перевод между своими картами.
     * <p>
     * Отправляется POST-запрос на эндпоинт <b>/api/transfers</b> с JSON-данными перевода.
     * Проверяется, что операция выполнена успешно и возвращается корректное сообщение.
     * </p>
     *
     * @throws Exception если происходит ошибка при выполнении HTTP-запроса
     */
    @Test
    void testTransferBetweenOwnCards() throws Exception {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromCardId(fromCard.getId());
        transferRequest.setToCardId(toCard.getId());
        transferRequest.setAmount(BigDecimal.valueOf(300));

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transfer completed successfully"));
    }

    /**
     * Тестирует попытку перевода с недостаточным балансом.
     * <p>
     * Отправляется POST-запрос на эндпоинт <b>/api/transfers</b> с суммой больше, чем баланс карты-отправителя.
     * Проверяется, что возвращается статус 400 и сообщение об ошибке.
     * </p>
     *
     * @throws Exception если происходит ошибка при выполнении HTTP-запроса
     */
    @Test
    void testTransferInsufficientFunds() throws Exception {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromCardId(fromCard.getId());
        transferRequest.setToCardId(toCard.getId());
        transferRequest.setAmount(BigDecimal.valueOf(2000));

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Insufficient funds"));
    }
}