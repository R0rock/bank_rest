package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Сервис для обработки переводов средств между банковскими картами пользователя.
 *
 * <p>Содержит бизнес-логику проверки корректности перевода, в том числе:
 * <ul>
 *   <li>Проверку принадлежности карт пользователю</li>
 *   <li>Проверку статуса карт (должны быть активны)</li>
 *   <li>Проверку баланса карты-отправителя</li>
 *   <li>Запрет перевода между одной и той же картой</li>
 * </ul>
 *
 * <p>Все операции выполняются в рамках транзакции благодаря аннотации {@link Transactional}.</p>
 *
 * <p>Используется контроллером {@link com.example.bankcards.controller.TransferController}.</p>
 */
@Service
@Transactional
public class TransferService {

    /** Репозиторий для доступа к данным о банковских картах. */
    @Autowired
    private CardRepository cardRepository;

    /** Сервис для работы с пользователями. */
    @Autowired
    private UserService userService;

    /**
     * Выполняет перевод средств между двумя картами одного пользователя.
     *
     * <p>Метод выполняет несколько проверок:
     * <ul>
     *   <li>Обе карты принадлежат текущему пользователю</li>
     *   <li>Обе карты активны</li>
     *   <li>Сумма перевода не превышает доступный баланс карты-отправителя</li>
     *   <li>Перевод не осуществляется на ту же самую карту</li>
     * </ul>
     * После прохождения всех проверок баланс списывается с карты-отправителя
     * и зачисляется на карту-получателя.</p>
     *
     * @param transferRequest объект {@link TransferRequest}, содержащий данные перевода
     * @param username имя пользователя, выполняющего перевод
     * @throws RuntimeException если одна из проверок не пройдена (например, недостаточно средств или карта неактивна)
     */
    public void transferBetweenOwnCards(TransferRequest transferRequest, String username) {
        User user = userService.findByUsername(username);

        // 1. Найти карту-отправителя и убедиться, что она принадлежит пользователю
        Card fromCard = cardRepository.findByIdAndUser(transferRequest.getFromCardId(), user)
                .orElseThrow(() -> new RuntimeException("Source card not found or doesn't belong to user"));

        // 2. Найти карту-получателя и убедиться, что она принадлежит пользователю
        Card toCard = cardRepository.findByIdAndUser(transferRequest.getToCardId(), user)
                .orElseThrow(() -> new RuntimeException("Destination card not found or doesn't belong to user"));

        // 3. Проверка на разные карты
        if (fromCard.getId().equals(toCard.getId())) {
            throw new RuntimeException("Cannot transfer to the same card");
        }

        // 4. Проверка статуса карт
        if (fromCard.getStatus() != Card.CardStatus.ACTIVE || toCard.getStatus() != Card.CardStatus.ACTIVE) {
            throw new RuntimeException("Both cards must be active for transfer");
        }

        // 5. Проверка баланса
        BigDecimal amount = transferRequest.getAmount();
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        // 6. Выполнение перевода
        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }
}