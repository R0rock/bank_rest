package com.example.bankcards.repository;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.Role.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Role}.
 * <p>
 * Предоставляет методы для взаимодействия с таблицей ролей пользователей.
 * Используется для управления ролями и их связью с пользователями.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Находит роль по её имени.
     * <p>
     * Например, {@code ROLE_USER} или {@code ROLE_ADMIN}.
     *
     * @param name имя роли из перечисления {@link RoleName}
     * @return {@link Optional}, содержащий найденную роль, если она существует
     */
    Optional<Role> findByName(RoleName name);
}