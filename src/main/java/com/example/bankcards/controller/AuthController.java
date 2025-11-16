package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.ApiResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtUtils;
import com.example.bankcards.security.UserDetailsImpl;
import com.example.bankcards.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.Set;

/**
 * Контроллер для обработки запросов, связанных с аутентификацией и регистрацией пользователей.
 * <p>
 * Отвечает за:
 * <ul>
 *     <li>Авторизацию пользователей и генерацию JWT-токенов.</li>
 *     <li>Регистрацию обычных пользователей.</li>
 *     <li>Регистрацию администраторов с повышенными правами доступа.</li>
 * </ul>
 * <p>
 * Все методы возвращают стандартный ответ {@link ApiResponse}, который содержит сообщение и (при необходимости) данные.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Менеджер аутентификации Spring Security, используемый для проверки логина и пароля.
     */
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Сервис для управления пользователями (создание, поиск и др.).
     */
    @Autowired
    private UserService userService;

    /**
     * Утилита для генерации и валидации JWT-токенов.
     */
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Выполняет аутентификацию пользователя по логину и паролю.
     * <p>
     * После успешной проверки данных:
     * <ul>
     *     <li>Создаёт JWT-токен для пользователя.</li>
     *     <li>Возвращает информацию о пользователе и токене.</li>
     * </ul>
     *
     * @param loginRequest объект {@link AuthRequest}, содержащий логин и пароль.
     * @return {@link ResponseEntity} с успешным ответом {@link ApiResponse}, содержащим {@link AuthResponse}.
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        AuthResponse authResponse = new AuthResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getFirstName(),
                userDetails.getLastName()
        );

        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    /**
     * Регистрирует нового пользователя с базовой ролью <b>ROLE_USER</b>.
     * <p>
     * В текущей реализации:
     * <ul>
     *     <li>Используется {@code username} из запроса для создания email-заглушки ({@code username@example.com}).</li>
     *     <li>Имя и фамилия задаются статически как "User" и "LastName".</li>
     * </ul>
     * В реальном проекте рекомендуется использовать отдельную DTO для регистрации с более полными данными.
     *
     * @param signUpRequest объект {@link AuthRequest}, содержащий имя пользователя и пароль.
     * @return {@link ResponseEntity} с сообщением об успешной регистрации.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody AuthRequest signUpRequest) {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");

        User user = userService.createUser(
                signUpRequest.getUsername(),
                signUpRequest.getPassword(),
                signUpRequest.getUsername() + "@example.com",
                "User",
                "LastName",
                roles
        );

        return ResponseEntity.ok(ApiResponse.success("User registered successfully"));
    }

    /**
     * Регистрирует нового пользователя с ролями <b>ROLE_ADMIN</b> и <b>ROLE_USER</b>.
     * <p>
     * Этот метод используется для создания учётных записей администраторов системы.
     * <ul>
     *     <li>Логин используется как имя пользователя и часть email-заглушки.</li>
     *     <li>Имя и фамилия задаются статически как "Admin" и "User".</li>
     * </ul>
     *
     * @param signUpRequest объект {@link AuthRequest}, содержащий имя пользователя и пароль.
     * @return {@link ResponseEntity} с сообщением об успешной регистрации администратора.
     */
    @PostMapping("/admin/signup")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody AuthRequest signUpRequest) {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        roles.add("ROLE_USER");

        User user = userService.createUser(
                signUpRequest.getUsername(),
                signUpRequest.getPassword(),
                signUpRequest.getUsername() + "@example.com",
                "Admin",
                "User",
                roles
        );

        return ResponseEntity.ok(ApiResponse.success("Admin user registered successfully"));
    }
}