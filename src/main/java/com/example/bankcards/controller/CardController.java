package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.ApiResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * REST контроллер для операций пользователя с банковскими картами.
 *
 * <p>Все методы доступны для пользователей с ролями USER и ADMIN.</p>
 *
 * <p>Основные функции:
 * <ul>
 *     <li>Просмотр всех карт пользователя с пагинацией и сортировкой</li>
 *     <li>Просмотр только активных карт</li>
 *     <li>Просмотр конкретной карты по ID</li>
 *     <li>Создание новой карты</li>
 *     <li>Блокировка и активация карты</li>
 *     <li>Удаление карты</li>
 *     <li>Подсчет количества карт пользователя</li>
 * </ul></p>
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cards")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class CardController {

    @Autowired
    private CardService cardService;

    /**
     * Получение всех карт текущего пользователя с пагинацией и сортировкой.
     *
     * @param page номер страницы (по умолчанию 0)
     * @param size размер страницы (по умолчанию 10)
     * @param sort поле для сортировки (по умолчанию "id")
     * @return ResponseEntity с сообщением и страницей объектов {@link CardResponse}
     */
    @GetMapping
    public ResponseEntity<?> getUserCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<CardResponse> cards = cardService.getUserCards(getCurrentUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Cards retrieved successfully", cards));
    }

    /**
     * Получение всех активных карт текущего пользователя.
     *
     * @return ResponseEntity с сообщением и списком {@link CardResponse}
     */
    @GetMapping("/active")
    public ResponseEntity<?> getUserActiveCards() {
        List<CardResponse> cards = cardService.getUserActiveCards(getCurrentUsername());
        return ResponseEntity.ok(ApiResponse.success("Active cards retrieved successfully", cards));
    }

    /**
     * Получение информации о конкретной карте пользователя по ID.
     *
     * @param id идентификатор карты
     * @return ResponseEntity с сообщением и объектом {@link CardResponse}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCard(@PathVariable Long id) {
        CardResponse card = cardService.getCardById(id, getCurrentUsername());
        return ResponseEntity.ok(ApiResponse.success("Card retrieved successfully", card));
    }

    /**
     * Создание новой карты для текущего пользователя.
     *
     * @param cardRequest объект {@link CardRequest}, содержащий данные новой карты
     * @return ResponseEntity с сообщением и объектом {@link CardResponse} для созданной карты
     */
    @PostMapping
    public ResponseEntity<?> createCard(@Valid @RequestBody CardRequest cardRequest) {
        Card card = cardService.createCardForUser(cardRequest, getCurrentUsername());
        return ResponseEntity.ok(ApiResponse.success("Card created successfully",
                new CardResponse(card.getId(), card.getCardNumberMasked(), card.getCardHolderName(),
                        card.getExpirationDate(), card.getStatus(), card.getBalance(),
                        card.getCreatedAt(), card.getUpdatedAt())));
    }

    /**
     * Блокировка карты пользователя по ID.
     *
     * @param id идентификатор карты
     * @return ResponseEntity с сообщением и объектом {@link CardResponse} после блокировки
     */
    @PutMapping("/{id}/block")
    public ResponseEntity<?> blockCard(@PathVariable Long id) {
        Card card = cardService.blockCard(id, getCurrentUsername());
        return ResponseEntity.ok(ApiResponse.success("Card blocked successfully",
                new CardResponse(card.getId(), card.getCardNumberMasked(), card.getCardHolderName(),
                        card.getExpirationDate(), card.getStatus(), card.getBalance(),
                        card.getCreatedAt(), card.getUpdatedAt())));
    }

    /**
     * Активация карты пользователя по ID.
     *
     * @param id идентификатор карты
     * @return ResponseEntity с сообщением и объектом {@link CardResponse} после активации
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateCard(@PathVariable Long id) {
        Card card = cardService.activateCard(id, getCurrentUsername());
        return ResponseEntity.ok(ApiResponse.success("Card activated successfully",
                new CardResponse(card.getId(), card.getCardNumberMasked(), card.getCardHolderName(),
                        card.getExpirationDate(), card.getStatus(), card.getBalance(),
                        card.getCreatedAt(), card.getUpdatedAt())));
    }

    /**
     * Удаление карты пользователя по ID.
     *
     * @param id идентификатор карты
     * @return ResponseEntity с сообщением об успешном удалении
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id, getCurrentUsername());
        return ResponseEntity.ok(ApiResponse.success("Card deleted successfully"));
    }

    /**
     * Получение количества карт текущего пользователя.
     *
     * @return ResponseEntity с сообщением и количеством карт
     */
    @GetMapping("/count")
    public ResponseEntity<?> countUserCards() {
        Long count = cardService.countUserCards(getCurrentUsername());
        return ResponseEntity.ok(ApiResponse.success("Card count retrieved successfully", count));
    }

    /**
     * Получение имени текущего пользователя из контекста безопасности.
     *
     * @return username текущего пользователя
     */
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}