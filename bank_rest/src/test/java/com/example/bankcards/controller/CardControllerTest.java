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
 * Тестовый класс для проверки работы {@link com.example.bankcards.controller.CardController}.
 *
 * <p>Использует {@link MockMvc} для эмуляции HTTP-запросов к REST API, связанных
 * с созданием и получением банковских карт пользователя.</p>
 *
 * <p>Основные сценарии тестирования:
 * <ul>
 *     <li>Создание карты для пользователя</li>
 *     <li>Получение списка карт пользователя</li>
 *     <li>Попытка доступа без авторизации</li>
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
public class CardControllerTest {

    /** MockMvc для имитации HTTP-запросов к REST API без запуска реального сервера. */
    @Autowired
    private MockMvc mockMvc;

    /** ObjectMapper используется для сериализации/десериализации JSON в тестах. */
    @Autowired
    private ObjectMapper objectMapper;

    /** Сервис карт, используется для взаимодействия с логикой создания карт. */
    @Autowired
    private CardService cardService;

    /** Репозиторий пользователей, используется для работы с тестовыми пользователями. */
    @Autowired
    private UserRepository userRepository;

    /** JWT-токен авторизованного тестового пользователя. */
    private String userToken;

    /** Тестовый пользователь для выполнения операций с картами. */
    private User testUser;

    /**
     * Инициализация перед каждым тестом.
     * <p>
     * Создается тестовый пользователь и выполняется авторизация, чтобы получить JWT-токен
     * для использования в запросах, требующих аутентификации.
     * </p>
     *
     * @throws Exception если происходит ошибка при выполнении HTTP-запроса
     */
    @BeforeEach
    void setUp() throws Exception {
        testUser = new User();
        testUser.setUsername("carduser");
        testUser.setPassword("password123");
        testUser.setEmail("carduser@example.com");
        testUser.setFirstName("Card");
        testUser.setLastName("User");

        AuthRequest registerRequest = new AuthRequest();
        registerRequest.setUsername("carduser");
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
    }

    /**
     * Тестирует создание карты для авторизованного пользователя.
     * <p>
     * Отправляется POST-запрос на эндпоинт <b>/api/cards</b> с JSON-данными карты.
     * Проверяется, что карта создана успешно и маскированный номер возвращается корректно.
     * </p>
     *
     * @throws Exception если происходит ошибка при выполнении HTTP-запроса
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
     * Тестирует получение списка карт пользователя.
     * <p>
     * Сначала создается карта, затем выполняется GET-запрос на <b>/api/cards</b>.
     * Проверяется, что ответ содержит массив карт и маскированный номер карты корректен.
     * </p>
     *
     * @throws Exception если происходит ошибка при выполнении HTTP-запроса
     */
    @Test
    void testGetUserCards() throws Exception {
        testCreateCardForUser();

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
     * Тестирует доступ к эндпоинту получения карт без авторизации.
     * <p>
     * Ожидается ответ 403 Forbidden, так как JWT-токен отсутствует.
     * </p>
     *
     * @throws Exception если происходит ошибка при выполнении HTTP-запроса
     */
    @Test
    void testGetCards_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }
}