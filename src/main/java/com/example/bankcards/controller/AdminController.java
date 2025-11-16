package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.UserProfile;
import com.example.bankcards.dto.ApiResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST контроллер для административных операций в приложении Bank Cards.
 *
 * <p>Все методы защищены и доступны только пользователям с ролью ADMIN.</p>
 *
 * <p>Основные функции:
 * <ul>
 *     <li>Просмотр всех пользователей</li>
 *     <li>Просмотр всех карт и отдельных карт</li>
 *     <li>Создание карты для пользователя</li>
 *     <li>Удаление пользователей и карт</li>
 * </ul></p>
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private CardService cardService;

    /**
     * Получение списка всех пользователей.
     *
     * @return ResponseEntity с сообщением и списком {@link UserProfile}
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<UserProfile> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    /**
     * Получение списка всех карт.
     *
     * @return ResponseEntity с сообщением и списком {@link CardResponse}
     */
    @GetMapping("/cards")
    public ResponseEntity<?> getAllCards() {
        List<CardResponse> cards = cardService.getAllCards();
        return ResponseEntity.ok(ApiResponse.success("Cards retrieved successfully", cards));
    }

    /**
     * Получение информации о конкретной карте по ID.
     *
     * @param id идентификатор карты
     * @return ResponseEntity с сообщением и объектом {@link CardResponse}
     */
    @GetMapping("/cards/{id}")
    public ResponseEntity<?> getCard(@PathVariable Long id) {
        CardResponse card = cardService.getCardByIdForAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Card retrieved successfully", card));
    }

    /**
     * Создание новой карты для указанного пользователя.
     *
     * @param cardRequest объект {@link CardRequest}, содержащий данные карты и username пользователя
     * @return ResponseEntity с сообщением и данными созданной карты {@link CardResponse}
     */
    @PostMapping("/cards")
    public ResponseEntity<?> createCardForUser(@Valid @RequestBody CardRequest cardRequest) {
        if (cardRequest.getUsername() == null || cardRequest.getUsername().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Username is required to create a card"));
        }
        Card card = cardService.createCard(cardRequest);
        return ResponseEntity.ok(ApiResponse.success(
                "Card created successfully for user " + cardRequest.getUsername(),
                new CardResponse(card.getId(), card.getCardNumberMasked(), card.getCardHolderName(),
                        card.getExpirationDate(), card.getStatus(), card.getBalance(),
                        card.getCreatedAt(), card.getUpdatedAt())));
    }

    /**
     * Удаление пользователя по ID.
     *
     * @param id идентификатор пользователя
     * @return ResponseEntity с сообщением об успешном удалении
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    /**
     * Удаление карты по ID.
     *
     * @param id идентификатор карты
     * @return ResponseEntity с сообщением об успешном удалении
     */
    @DeleteMapping("/cards/{id}")
    public ResponseEntity<?> deleteCard(@PathVariable Long id) {
        cardService.deleteCardByAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Card deleted successfully"));
    }
}