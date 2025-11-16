package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для загрузки пользовательских данных для аутентификации.
 * <p>
 * Реализует интерфейс {@link UserDetailsService} Spring Security.
 * Используется для получения {@link UserDetails} по имени пользователя (логину) при аутентификации.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Загружает пользователя по имени пользователя (логину) из базы данных.
     *
     * @param username имя пользователя для поиска
     * @return объект {@link UserDetails} с данными пользователя и ролями
     * @throws UsernameNotFoundException если пользователь с указанным именем не найден
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        return UserDetailsImpl.build(user);
    }
}