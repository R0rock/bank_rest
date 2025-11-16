package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Card}.
 * <p>
 * Предоставляет методы для выполнения операций CRUD и
 * пользовательских запросов, связанных с банковскими картами пользователей.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    /**
     * Возвращает страницу карт, принадлежащих указанному пользователю.
     *
     * @param user     пользователь, чьи карты нужно получить
     * @param pageable объект пагинации
     * @return страница с картами пользователя
     */
    Page<Card> findByUser(User user, Pageable pageable);

    /**
     * Возвращает список всех карт, принадлежащих указанному пользователю.
     *
     * @param user пользователь, чьи карты нужно получить
     * @return список карт пользователя
     */
    List<Card> findByUser(User user);

    /**
     * Ищет карту по её идентификатору и пользователю.
     * <p>
     * Используется для проверки прав доступа пользователя к карте.
     *
     * @param id   идентификатор карты
     * @param user пользователь, которому должна принадлежать карта
     * @return {@link Optional}, содержащий карту, если она найдена и принадлежит пользователю
     */
    Optional<Card> findByIdAndUser(Long id, User user);

    /**
     * Возвращает список всех активных карт пользователя.
     *
     * @param user пользователь, для которого выполняется поиск
     * @return список активных карт
     */
    @Query("SELECT c FROM Card c WHERE c.user = :user AND c.status = 'ACTIVE'")
    List<Card> findActiveCardsByUser(@Param("user") User user);

    /**
     * Подсчитывает количество карт, принадлежащих указанному пользователю.
     *
     * @param user пользователь, чьи карты нужно посчитать
     * @return количество карт пользователя
     */
    Long countByUser(User user);
}