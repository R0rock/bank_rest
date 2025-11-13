package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тестовый класс для проверки работы {@link com.example.bankcards.controller.AuthController}.
 *
 * <p>Использует {@link MockMvc} для эмуляции HTTP-запросов и проверки
 * REST API эндпоинтов, связанных с аутентификацией и регистрацией пользователей.</p>
 *
 * <p>Основные сценарии, которые покрывает данный тест:
 * <ul>
 *     <li>Регистрация нового пользователя</li>
 *     <li>Авторизация обычного пользователя</li>
 *     <li>Авторизация администратора (предустановленного в Liquibase data YAML)</li>
 * </ul>
 * </p>
 *
 * <p>Каждый тест выполняется в отдельной транзакции, которая автоматически откатывается
 * после завершения теста благодаря аннотации {@link Transactional}.</p>
 *
 * <p>Активируется профиль {@code test}, что позволяет использовать тестовую БД или изолированные настройки.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Откатывать транзакции после каждого теста
@ActiveProfiles("test") // Использует профиль "test" для изоляции окружения
public class AuthControllerTest {

    /** MockMvc используется для имитации HTTP-запросов к REST API без поднятия реального сервера. */
    @Autowired
    private MockMvc mockMvc;

    /** ObjectMapper используется для сериализации и десериализации JSON в тестах. */
    @Autowired
    private ObjectMapper objectMapper;

    /** UserService используется для предварительного создания пользователей в тестах. */
    @Autowired
    private UserService userService;

    /** Репозиторий пользователей, при необходимости можно использовать для прямой проверки БД. */
    @Autowired
    private UserRepository userRepository;

    /** Кодировщик паролей, используемый системой аутентификации. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Тестирует регистрацию нового пользователя.
     * <p>
     * Отправляет POST-запрос на эндпоинт <b>/api/auth/signup</b> с JSON-запросом,
     * содержащим имя пользователя и пароль. Проверяет, что ответ успешен и содержит
     * сообщение о регистрации.
     * </p>
     *
     * @throws Exception если происходит ошибка при выполнении HTTP-запроса
     */
    @Test
    void testRegisterUser() throws Exception {
        AuthRequest registerRequest = new AuthRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    /**
     * Тестирует процесс логина обычного пользователя.
     * <p>
     * Сначала создается тестовый пользователь с помощью {@link UserService},
     * затем выполняется POST-запрос на эндпоинт <b>/api/auth/signin</b>.
     * Проверяется, что логин проходит успешно и в ответе возвращается JWT-токен.
     * </p>
     *
     * @throws Exception если происходит ошибка при выполнении HTTP-запроса
     */
    @Test
    void testLoginUser() throws Exception {
        // Сначала создаем пользователя для входа
        AuthRequest registerRequest = new AuthRequest();
        registerRequest.setUsername("loginuser");
        registerRequest.setPassword("password123");

        userService.createUser(
                "loginuser",
                "password123",
                "login@example.com",
                "Login", "User", null);

        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setUsername("loginuser");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").exists());
    }

    /**
     * Тестирует авторизацию администратора.
     * <p>
     * Использует учетные данные администратора, заданные в Liquibase-скрипте
     * <b>002-initial-data.yaml</b>. Проверяет, что при успешной аутентификации
     * возвращается JWT-токен и имя пользователя <code>admin</code>.
     * </p>
     *
     * @throws Exception если происходит ошибка при выполнении HTTP-запроса
     */
    @Test
    void testLoginAdmin() throws Exception {
        // Используем admin'а, созданного в 002-initial-data.yaml
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.username").value("admin"));
    }
}