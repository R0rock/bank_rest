package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/**
 * Интеграционные тесты контроллера управления банковскими картами {@link com.example.bankcards.controller.CardController}.
 *
 * Проверяются основные операции:
 * <ul>
 *     <li>Создание новой карты для авторизованного пользователя</li>
 *     <li>Получение списка карт пользователя</li>
 *     <li>Доступ к списку карт без авторизации (ожидается отказ)</li>
 * </ul>
 *
 * Тесты выполняются через MockMvc и включают реальную регистрацию, авторизацию и использование JWT.
 */

/**
 * Тестовый класс, проверяющий работу CardController:
 * <ol>
 *     <li>Создание карты пользователю</li>
 *     <li>Получение списка карт пользователя</li>
 *     <li>Запрет доступа к картам без авторизации</li>
 * </ol>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CardService cardService;

    @Autowired
    private UserRepository userRepository;

    /** JWT токен авторизованного пользователя */
    private String userToken;

    /** Тестовый пользователь */
    private User testUser;

    /**
     * Инициализация перед каждым тестом.
     * <ul>
     *     <li>Создаёт тестового пользователя</li>
     *     <li>Регистрирует его через REST API</li>
     *     <li>Авторизует и получает JWT токен</li>
     * </ul>
     *
     * @throws Exception если запрос MockMvc завершился ошибкой
     */
    @BeforeEach
    void setUp() throws Exception {
        testUser = new User();
        testUser.setUsername("carduser");
        testUser.setPassword("password123");
        testUser.setEmail("carduser@example.com");
        testUser.setFirstName("Card");
        testUser.setLastName("User");

        // Регистрация пользователя
        AuthRequest registerRequest = new AuthRequest();
        registerRequest.setUsername("carduser");
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

        // Получение JWT
        String responseBody = loginResult.getResponse().getContentAsString();
        userToken = "Bearer " + objectMapper.readTree(responseBody)
                .path("data").path("token").asText();
    }

    /**
     * Проверяет создание карты для авторизованного пользователя.
     *
     * Ожидания:
     * <ul>
     *     <li>HTTP 200</li>
     *     <li>success = true</li>
     *     <li>Маскированный номер карты корректно сформирован</li>
     * </ul>
     *
     * @throws Exception если MockMvc вызывает ошибку
     */
    @Test
    void testCreateCardForUser() throws Exception {
        CardRequest cardRequest = new CardRequest();
        cardRequest.setCardNumber("1234567812345670");
        cardRequest.setCardHolderName("Card User");
        cardRequest.setExpirationDate(LocalDate.now().plusYears(2));
        cardRequest.setBalance(BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/cards")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cardNumberMasked").value("**** **** **** 5670"));
    }

    /**
     * Проверяет получение списка карт пользователя.
     * Тест вызывает сначала {@link #testCreateCardForUser()}, затем запрашивает список.
     *
     * Ожидания:
     * <ul>
     *     <li>HTTP 200</li>
     *     <li>success = true</li>
     *     <li>Вернулся массив карт</li>
     *     <li>Первая карта имеет корректно замаскированный номер</li>
     * </ul>
     *
     * @throws Exception если запрос завершился ошибкой
     */
    @Test
    void testGetUserCards() throws Exception {
        // Сначала создаем карту
        testCreateCardForUser();

        // Затем получаем список
        mockMvc.perform(get("/api/cards")
                        .header("Authorization", userToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].cardNumberMasked").value("**** **** **** 5670"));
    }

    /**
     * Проверяет, что доступ к списку карт запрещён
     * при отсутствии заголовка Authorization.
     *
     * Ожидания:
     * <ul>
     *     <li>HTTP 403 Forbidden</li>
     * </ul>
     *
     * @throws Exception если MockMvc вызывает ошибку
     */
    @Test
    void testGetCards_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }
}