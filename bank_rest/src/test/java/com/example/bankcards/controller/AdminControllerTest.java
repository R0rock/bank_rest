package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.service.UserService;
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
 * Тестовый класс {@code AdminControllerTest} предназначен для проверки REST-эндпоинтов административного контроллера,
 * обеспечивающего управление пользователями и банковскими картами.
 *
 * <p>Класс использует:
 * <ul>
 *     <li>{@link SpringBootTest} — для запуска интеграционных тестов в контексте Spring Boot.</li>
 *     <li>{@link AutoConfigureMockMvc} — для работы с {@link MockMvc} без запуска сервера.</li>
 *     <li>{@link Transactional} — чтобы все изменения базы данных откатывались после каждого теста.</li>
 *     <li>{@link ActiveProfiles}("test") — чтобы использовать тестовый профиль конфигурации.</li>
 * </ul>
 *
 * <p>Перед выполнением тестов выполняется аутентификация администратора и создание тестового пользователя.
 *
 * @author
 *     Sergei
 * @version
 *     1.0
 * @since
 *     2025-11-12
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class AdminControllerTest {

    /** MockMvc используется для эмуляции HTTP-запросов без запуска сервера. */
    @Autowired
    private MockMvc mockMvc;

    /** ObjectMapper используется для сериализации/десериализации JSON. */
    @Autowired
    private ObjectMapper objectMapper;

    /** Сервис пользователей — для создания тестового пользователя. */
    @Autowired
    private UserService userService;

    /** JWT токен администратора, получаемый перед тестами. */
    private String adminToken;

    /**
     * Метод {@code setUp()} выполняется перед каждым тестом.
     * <p>Он:
     * <ul>
     *     <li>Авторизует пользователя {@code admin} и сохраняет его JWT токен.</li>
     *     <li>Создаёт тестового пользователя {@code userForAdminTest}.</li>
     * </ul>
     *
     * @throws Exception если возникают ошибки при выполнении HTTP-запросов.
     */
    @BeforeEach
    void setUp() throws Exception {
        // Логинимся как admin (создан в Liquibase-скрипте 002-initial-data.yaml)
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        adminToken = "Bearer " + objectMapper.readTree(responseBody)
                .path("data")
                .path("token")
                .asText();

        // Создаем тестового пользователя для административных операций
        userService.createUser(
                "userForAdminTest",
                "password123",
                "admin_test@example.com",
                "Test",
                "User",
                null
        );
    }

    /**
     * Тест {@code testGetAllUsersAsAdmin()} проверяет возможность администратора
     * получить список всех пользователей системы.
     *
     * <p>Проверяется:
     * <ul>
     *     <li>Возврат успешного HTTP-статуса (200 OK).</li>
     *     <li>Наличие поля {@code success} со значением {@code true}.</li>
     *     <li>Наличие в списке пользователей — {@code admin} и {@code userForAdminTest}.</li>
     * </ul>
     *
     * @throws Exception если запрос завершился неуспешно.
     */
    @Test
    void testGetAllUsersAsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                // Проверяем, что в списке есть и admin, и новый пользователь
                .andExpect(jsonPath("$.data[?(@.username == 'admin')]").exists())
                .andExpect(jsonPath("$.data[?(@.username == 'userForAdminTest')]").exists());
    }

    /**
     * Тест {@code testCreateCardForUserAsAdmin()} проверяет создание новой банковской карты
     * администратором от имени другого пользователя.
     *
     * <p>Проверяется:
     * <ul>
     *     <li>Успешное выполнение запроса.</li>
     *     <li>Сообщение о создании карты.</li>
     *     <li>Корректная маска номера карты в ответе.</li>
     * </ul>
     *
     * @throws Exception если возникает ошибка выполнения запроса.
     */
    @Test
    void testCreateCardForUserAsAdmin() throws Exception {
        CardRequest cardRequest = new CardRequest();
        cardRequest.setCardNumber("9876543210987654"); // Валидный номер
        cardRequest.setCardHolderName("Test User");
        cardRequest.setExpirationDate(LocalDate.now().plusYears(3));
        cardRequest.setBalance(BigDecimal.valueOf(500));
        cardRequest.setUsername("userForAdminTest");

        mockMvc.perform(post("/api/admin/cards")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Card created successfully for user userForAdminTest"))
                .andExpect(jsonPath("$.data.cardNumberMasked").value("**** **** **** 7654"));
    }

    /**
     * Тест {@code testGetAllUsers_Unauthorized()} проверяет,
     * что доступ к административным эндпоинтам без JWT токена запрещён.
     *
     * <p>Ожидается статус {@code 403 Forbidden}.
     *
     * @throws Exception если запрос не выполняется.
     */
    @Test
    void testGetAllUsers_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }
}