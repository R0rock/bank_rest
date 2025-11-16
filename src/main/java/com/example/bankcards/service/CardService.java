package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardNumberUtil;
import com.example.bankcards.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления банковскими картами.
 *
 * <p>Предоставляет методы для создания, блокировки, активации, удаления карт,
 * а также для получения информации о картах пользователя или всех карт (для администратора).</p>
 */
@Service
@Transactional
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardNumberUtil cardNumberUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private DateUtils dateUtils;

    /**
     * Создает карту для указанного пользователя (администратор).
     *
     * @param cardRequest данные карты, включая username пользователя
     * @return созданная карта
     * @throws RuntimeException если username не найден или номер карты некорректен
     */
    public Card createCard(CardRequest cardRequest) {
        User user = userService.findByUsername(cardRequest.getUsername());
        return createCardInternal(cardRequest, user);
    }

    /**
     * Создает карту для текущего пользователя.
     *
     * @param cardRequest данные карты
     * @param username имя пользователя
     * @return созданная карта
     * @throws RuntimeException если пользователь не найден или номер карты некорректен
     */
    public Card createCardForUser(CardRequest cardRequest, String username) {
        User user = userService.findByUsername(username);
        return createCardInternal(cardRequest, user);
    }

    /**
     * Блокирует карту пользователя.
     *
     * @param cardId идентификатор карты
     * @param username имя пользователя
     * @return обновленная карта с статусом BLOCKED
     * @throws RuntimeException если карта не найдена или истекла
     */
    public Card blockCard(Long cardId, String username) {
        User user = userService.findByUsername(username);
        Card card = cardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));
        if (card.getStatus() == Card.CardStatus.EXPIRED) {
            throw new RuntimeException("Cannot block an expired card");
        }
        card.setStatus(Card.CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    /**
     * Активирует карту пользователя.
     *
     * @param cardId идентификатор карты
     * @param username имя пользователя
     * @return обновленная карта с статусом ACTIVE
     * @throws RuntimeException если карта не найдена или истекла
     */
    public Card activateCard(Long cardId, String username) {
        User user = userService.findByUsername(username);
        Card card = cardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));
        if (card.getExpirationDate().isBefore(LocalDate.now())) {
            card.setStatus(Card.CardStatus.EXPIRED);
            cardRepository.save(card);
            throw new RuntimeException("Cannot activate an expired card");
        }
        card.setStatus(Card.CardStatus.ACTIVE);
        return cardRepository.save(card);
    }

    /**
     * Удаляет карту текущего пользователя.
     *
     * @param cardId идентификатор карты
     * @param username имя пользователя
     * @throws RuntimeException если карта не найдена
     */
    public void deleteCard(Long cardId, String username) {
        User user = userService.findByUsername(username);
        Card card = cardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));
        cardRepository.delete(card);
    }

    /**
     * Удаляет карту администратором.
     *
     * @param cardId идентификатор карты
     * @throws RuntimeException если карта не найдена
     */
    public void deleteCardByAdmin(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new RuntimeException("Card not found with id: " + cardId);
        }
        cardRepository.deleteById(cardId);
    }

    /**
     * Возвращает страницу карт пользователя.
     *
     * @param username имя пользователя
     * @param pageable параметры пагинации и сортировки
     * @return страница с картами пользователя
     */
    public Page<CardResponse> getUserCards(String username, Pageable pageable) {
        User user = userService.findByUsername(username);
        Page<Card> cards = cardRepository.findByUser(user, pageable);
        return cards.map(this::convertToCardResponse);
    }

    /**
     * Возвращает список активных карт пользователя.
     *
     * @param username имя пользователя
     * @return список активных карт
     */
    public List<CardResponse> getUserActiveCards(String username) {
        User user = userService.findByUsername(username);
        List<Card> cards = cardRepository.findActiveCardsByUser(user);
        return cards.stream().map(this::convertToCardResponse).collect(Collectors.toList());
    }

    /**
     * Возвращает карту по идентификатору для пользователя.
     *
     * @param cardId идентификатор карты
     * @param username имя пользователя
     * @return карта пользователя
     * @throws RuntimeException если карта не найдена
     */
    public CardResponse getCardById(Long cardId, String username) {
        User user = userService.findByUsername(username);
        Card card = cardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));
        return convertToCardResponse(card);
    }

    /**
     * Возвращает карту по идентификатору для администратора.
     *
     * @param cardId идентификатор карты
     * @return карта
     * @throws RuntimeException если карта не найдена
     */
    public CardResponse getCardByIdForAdmin(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));
        return convertToCardResponse(card);
    }

    /**
     * Возвращает список всех карт.
     *
     * @return список всех карт
     */
    public List<CardResponse> getAllCards() {
        return cardRepository.findAll().stream()
                .map(this::convertToCardResponse)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает количество карт пользователя.
     *
     * @param username имя пользователя
     * @return количество карт
     */
    public Long countUserCards(String username) {
        User user = userService.findByUsername(username);
        return cardRepository.countByUser(user);
    }

    // --- Вспомогательные методы ---

    /**
     * Конвертирует сущность Card в DTO CardResponse.
     *
     * @param card сущность карты
     * @return DTO с данными карты
     */
    private CardResponse convertToCardResponse(Card card) {
        return new CardResponse(
                card.getId(),
                card.getCardNumberMasked(),
                card.getCardHolderName(),
                card.getExpirationDate(),
                card.getStatus(),
                card.getBalance(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }

    /**
     * Проверяет корректность номера карты по алгоритму Луна.
     *
     * @param cardNumber номер карты
     * @return true если номер валиден, false иначе
     */
    private boolean isValidCardNumber(String cardNumber) {
        String cleanNumber = cardNumber.replaceAll("\\s+", "");
        if (!cleanNumber.matches("\\d+")) {
            return false;
        }
        int sum = 0;
        boolean alternate = false;
        for (int i = cleanNumber.length() - 1; i >= 0; i--) {
            int digit = Integer.parseInt(cleanNumber.substring(i, i + 1));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    /**
     * Внутренний метод для создания карты для указанного пользователя.
     *
     * @param cardRequest данные карты
     * @param user пользователь
     * @return созданная карта
     */
    private Card createCardInternal(CardRequest cardRequest, User user) {
        if (!isValidCardNumber(cardRequest.getCardNumber())) {
            throw new RuntimeException("Invalid card number");
        }

        Card.CardStatus status = cardRequest.getExpirationDate().isBefore(LocalDate.now())
                ? Card.CardStatus.EXPIRED
                : Card.CardStatus.ACTIVE;

        Card card = new Card();
        card.setCardNumberEncrypted(cardNumberUtil.encrypt(cardRequest.getCardNumber()));
        card.setCardNumberMasked(cardNumberUtil.maskCardNumber(cardRequest.getCardNumber()));
        card.setCardHolderName(cardRequest.getCardHolderName());
        card.setExpirationDate(cardRequest.getExpirationDate());
        card.setBalance(cardRequest.getBalance());
        card.setStatus(status);
        card.setUser(user);
        return cardRepository.save(card);
    }
}