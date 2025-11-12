package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link User}.
 * <p>
 * Предоставляет интерфейс для выполнения CRUD-операций и пользовательских запросов,
 * связанных с управлением пользователями системы.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по имени пользователя (логину).
     *
     * @param username имя пользователя
     * @return {@link Optional}, содержащий пользователя, если найден
     */
    Optional<User> findByUsername(String username);

    /**
     * Находит пользователя по адресу электронной почты.
     *
     * @param email адрес электронной почты
     * @return {@link Optional}, содержащий пользователя, если найден
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверяет, существует ли пользователь с указанным именем пользователя.
     *
     * @param username имя пользователя для проверки
     * @return {@code true}, если пользователь с таким именем существует; иначе {@code false}
     */
    Boolean existsByUsername(String username);

    /**
     * Проверяет, существует ли пользователь с указанным адресом электронной почты.
     *
     * @param email адрес электронной почты для проверки
     * @return {@code true}, если пользователь с таким email существует; иначе {@code false}
     */
    Boolean existsByEmail(String email);
}