package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Реализация {@link UserDetails} для работы с пользователем системы.
 * <p>
 * Содержит основную информацию о пользователе, включая идентификатор, имя пользователя,
 * email, пароль, роли (authorities) и персональные данные.
 * Используется Spring Security для аутентификации и авторизации.
 */
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    /**
     * Уникальный идентификатор пользователя.
     */
    private Long id;

    /**
     * Имя пользователя (логин).
     */
    private String username;

    /**
     * Адрес электронной почты пользователя.
     */
    private String email;

    /**
     * Зашифрованный пароль пользователя.
     */
    @JsonIgnore
    private String password;

    /**
     * Коллекция ролей/прав пользователя.
     */
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Имя пользователя.
     */
    private String firstName;

    /**
     * Фамилия пользователя.
     */
    private String lastName;

    /**
     * Конструктор для создания объекта UserDetailsImpl.
     *
     * @param id          идентификатор пользователя
     * @param username    имя пользователя
     * @param email       email пользователя
     * @param password    пароль пользователя
     * @param firstName   имя
     * @param lastName    фамилия
     * @param authorities коллекция ролей и прав
     */
    public UserDetailsImpl(Long id, String username, String email, String password,
                           String firstName, String lastName,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.authorities = authorities;
    }

    /**
     * Статический метод для создания {@link UserDetailsImpl} на основе сущности {@link User}.
     *
     * @param user объект пользователя
     * @return объект UserDetailsImpl с заполненными полями и ролями
     */
    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getFirstName(),
                user.getLastName(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Получить идентификатор пользователя.
     *
     * @return id пользователя
     */
    public Long getId() {
        return id;
    }

    /**
     * Получить email пользователя.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Получить имя пользователя.
     *
     * @return firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Получить фамилию пользователя.
     *
     * @return lastName
     */
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Сравнивает двух пользователей по идентификатору.
     *
     * @param o другой объект
     * @return true, если объекты представляют одного и того же пользователя
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}