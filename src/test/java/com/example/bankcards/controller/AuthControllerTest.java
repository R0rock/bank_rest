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
 * Интеграционные тесты для {@code AuthController}, отвечающего
 * за регистрацию и авторизацию пользователей.
 *
 * <p>Основные проверяемые функции:</p>
 * <ul>
 *     <li>Регистрация нового пользователя</li>
 *     <li>Авторизация существующего пользователя</li>
 * </ul>
 *
 * <p>Выполняется под профилем {@code test} с предварительной
 * фиксацией пароля администратора через SQL-скрипт.</p>
 */

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Тестирует успешную регистрацию пользователя через эндпоинт:
     * <pre>/api/auth/signup</pre>
     *
     * <p>Ожидает:</p>
     * <ul>
     *     <li>HTTP 200</li>
     *     <li>success = true</li>
     *     <li>message = "User registered successfully"</li>
     * </ul>
     *
     * @throws Exception при выполнении MockMvc-запроса
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
     * Тестирует логин пользователя, который предварительно создаётся напрямую
     * через {@link UserService#createUser}.
     *
     * <p>Ожидает:</p>
     * <ul>
     *     <li>HTTP 200</li>
     *     <li>success = true</li>
     *     <li>message = "Login successful"</li>
     *     <li>наличие JWT-токена в поле data.token</li>
     * </ul>
     *
     * @throws Exception при выполнении MockMvc-запроса
     */
    @Test
    void testLoginUser() throws Exception {
        // Создаем пользователя
        AuthRequest registerRequest = new AuthRequest();
        registerRequest.setUsername("loginuser");
        registerRequest.setPassword("password123");
        userService.createUser(
                "loginuser",
                "password123",
                "login@example.com",
                "Login",
                "User",
                null);

        // Пытаемся войти
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
}