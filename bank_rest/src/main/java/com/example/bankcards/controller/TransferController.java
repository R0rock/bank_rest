package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.ApiResponse;
import com.example.bankcards.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * Контроллер для обработки операций перевода средств между картами пользователя.
 *
 * <p>Предоставляет REST API для перевода денег между собственными картами
 * аутентифицированного пользователя. Доступ к операциям ограничен ролью {@code USER}.</p>
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    @Autowired
    private TransferService transferService;

    /**
     * Выполняет перевод средств между картами текущего пользователя.
     *
     * <p>Требуется авторизация пользователя с ролью {@code USER}. Метод принимает запрос
     * {@link TransferRequest} с указанием номера карты-источника, карты-получателя
     * и суммы перевода.</p>
     *
     * @param transferRequest данные перевода (карты и сумма)
     * @return ответ с сообщением об успешном завершении перевода
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> transferBetweenOwnCards(@Valid @RequestBody TransferRequest transferRequest) {
        transferService.transferBetweenOwnCards(transferRequest, getCurrentUsername());
        return ResponseEntity.ok(ApiResponse.success("Transfer completed successfully"));
    }

    /**
     * Извлекает имя текущего аутентифицированного пользователя из контекста безопасности.
     *
     * @return имя пользователя, выполняющего запрос
     */
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}