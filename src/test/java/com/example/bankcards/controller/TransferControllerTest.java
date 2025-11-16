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
 * Тесты контроллера переводов {@link com.example.bankcards.controller.TransferController}.
 * Проверяются:
 * <ul>
 *     <li>Успешный перевод между собственными картами пользователя</li>
 *     <li>Ошибка при недостаточном балансе</li>
 * </ul>
 * Тесты выполняются через MockMvc, включая реальную аутентификацию и создание карт.
 */

/**
 * Интеграционные тесты TransferController.
 * Тестируется полный цикл:
 * <ol>
 *     <li>Регистрация пользователя</li>
 *     <li>Авторизация и получение JWT</li>
 *     <li>Создание двух карт пользователя</li>
 *     <li>Выполнение перевода</li>
 *     <li>Проверка обработки ошибок</li>
 * </ol>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CardService cardService;

    /** JWT токен тестового пользователя */
    private String userToken;

    /** Карта-источник средств */
    private Card fromCard;

    /** Карта-получатель средств */
    private Card toCard;

    /**
     * Подготовка окружения для каждого теста.
     * Включает:
     * <ul>
     *     <li>Регистрацию пользователя</li>
     *     <li>Авторизацию и получение JWT</li>
     *     <li>Создание двух карт через CardService</li>
     * </ul>
     *
     * @throws Exception если MockMvc-запросы не проходят
     */
    @BeforeEach
    void setUp() throws Exception {

        // Регистрация
        AuthRequest registerRequest = new AuthRequest();
        registerRequest.setUsername("transferuser");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Авторизация
        MvcResult loginResult = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        userToken = "Bearer " + objectMapper.readTree(responseBody)
                .path("data").path("token").asText();

        // Создание карт
        CardRequest cardRequest1 = new CardRequest();
        cardRequest1.setCardNumber("1234567812345670");
        cardRequest1.setCardHolderName("Transfer User");
        cardRequest1.setExpirationDate(LocalDate.now().plusYears(2));
        cardRequest1.setBalance(BigDecimal.valueOf(1000));
        fromCard = cardService.createCardForUser(cardRequest1, "transferuser");

        CardRequest cardRequest2 = new CardRequest();
        cardRequest2.setCardNumber("1234567812345688");
        cardRequest2.setCardHolderName("Transfer User");
        cardRequest2.setExpirationDate(LocalDate.now().plusYears(3));
        cardRequest2.setBalance(BigDecimal.valueOf(500));
        toCard = cardService.createCardForUser(cardRequest2, "transferuser");
    }

    /**
     * Проверяет успешный перевод между картами одного пользователя.
     *
     * Ожидания:
     * <ul>
     *     <li>HTTP 200</li>
     *     <li>Поле success = true</li>
     *     <li>Поле message = "Transfer completed successfully"</li>
     * </ul>
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
     * Проверяет ошибку при попытке перевести сумму,
     * превышающую баланс карты-источника.
     *
     * Ожидания:
     * <ul>
     *     <li>HTTP 400</li>
     *     <li>Поле success = false</li>
     *     <li>Поле message = "Insufficient funds"</li>
     * </ul>
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