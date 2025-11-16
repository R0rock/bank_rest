package com.example.bankcards.service;

import com.example.bankcards.dto.UserProfile;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис для работы с пользователями системы.
 * <p>
 * Предоставляет методы для создания, поиска, удаления пользователей,
 * а также получения информации о профиле и списке пользователей.
 * Используется на уровне бизнес-логики для управления сущностью {@link User}.
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Создает нового пользователя с указанными данными и ролями.
     *
     * @param username  логин пользователя
     * @param password  пароль пользователя
     * @param email     email пользователя
     * @param firstName имя пользователя
     * @param lastName  фамилия пользователя
     * @param roleNames набор названий ролей, например {"ROLE_USER", "ROLE_ADMIN"}
     * @return сохраненный объект {@link User}
     * @throws RuntimeException если имя пользователя или email уже заняты
     *                          или указанная роль не найдена
     */
    public User createUser(String username, String password, String email,
                           String firstName, String lastName, Set<String> roleNames) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already in use!");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        Set<Role> roles = new HashSet<>();
        if (roleNames == null || roleNames.isEmpty()) {
            Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
            roles.add(userRole);
        } else {
            roleNames.forEach(roleName -> {
                Role.RoleName name = Role.RoleName.valueOf(roleName);
                Role role = roleRepository.findByName(name)
                        .orElseThrow(() -> new RuntimeException("Error: Role " + roleName + " is not found."));
                roles.add(role);
            });
        }
        user.setRoles(roles);
        return userRepository.save(user);
    }

    /**
     * Находит пользователя по имени пользователя (логину).
     *
     * @param username имя пользователя
     * @return объект {@link User}
     * @throws RuntimeException если пользователь не найден
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    /**
     * Находит пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return объект {@link User}
     * @throws RuntimeException если пользователь не найден
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    /**
     * Возвращает список всех пользователей в виде профилей {@link UserProfile}.
     *
     * @return список профилей пользователей
     */
    public List<UserProfile> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserProfile)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает профиль пользователя по имени пользователя.
     *
     * @param username имя пользователя
     * @return объект {@link UserProfile} с информацией о пользователе
     */
    public UserProfile getUserProfile(String username) {
        User user = findByUsername(username);
        return convertToUserProfile(user);
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @throws RuntimeException если пользователь не найден
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Преобразует сущность {@link User} в объект {@link UserProfile}.
     *
     * @param user объект {@link User}
     * @return объект {@link UserProfile} с данными пользователя
     */
    private UserProfile convertToUserProfile(User user) {
        UserProfile profile = new UserProfile();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setEmail(user.getEmail());
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setCreatedAt(user.getCreatedAt());
        return profile;
    }
}