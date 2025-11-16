package com.example.bankcards.config;

import com.example.bankcards.security.AuthTokenFilter;
import com.example.bankcards.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Конфигурация безопасности Spring Security для REST API.
 * <p>
 * Основные функции:
 * <ul>
 *     <li>Настройка фильтра JWT-аутентификации.</li>
 *     <li>Отключение состояния сессии (статлес режим).</li>
 *     <li>Настройка CORS и CSRF.</li>
 *     <li>Разрешение публичных эндпоинтов для авторизации, Swagger и Actuator.</li>
 * </ul>
 * <p>
 * Используется {@link UserDetailsServiceImpl} для загрузки пользователей
 * и {@link AuthTokenFilter} для проверки JWT-токенов в каждом запросе.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * Сервис для загрузки пользовательских данных из источника (например, базы данных).
     */
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * Создаёт и настраивает фильтр для обработки JWT-токенов в запросах.
     *
     * @return экземпляр {@link AuthTokenFilter}.
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * Настраивает провайдер аутентификации, использующий {@link UserDetailsServiceImpl}
     * и {@link BCryptPasswordEncoder} для проверки паролей.
     *
     * @return экземпляр {@link DaoAuthenticationProvider}.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Предоставляет менеджер аутентификации, который используется при авторизации пользователей.
     *
     * @param authConfig конфигурация аутентификации Spring Security.
     * @return экземпляр {@link AuthenticationManager}.
     * @throws Exception если не удаётся создать {@link AuthenticationManager}.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Кодировщик паролей, основанный на алгоритме BCrypt.
     * Используется для хэширования и проверки паролей пользователей.
     *
     * @return экземпляр {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Основная конфигурация фильтров безопасности и политики доступа.
     * <p>
     * - Отключает CSRF и включает CORS.<br>
     * - Переводит приложение в статлес режим (без хранения сессий).<br>
     * - Разрешает доступ без авторизации к эндпоинтам:
     *   <ul>
     *       <li>/api/auth/** — эндпоинты для аутентификации и регистрации;</li>
     *       <li>/swagger-ui/**, /v3/api-docs/**, /api-docs/** — Swagger-документация;</li>
     *       <li>/actuator/** — метрики и health-check для Docker.</li>
     *   </ul>
     * - Все остальные запросы требуют авторизации.
     *
     * @param http объект {@link HttpSecurity} для конфигурации фильтров и правил доступа.
     * @return экземпляр {@link SecurityFilterChain}, определяющий правила безопасности.
     * @throws Exception если при настройке возникает ошибка.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll() // Для health check в Docker
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Настройка CORS-фильтра, разрешающего запросы со всех источников.
     * <p>
     * Позволяет всем доменам, заголовкам и методам обращаться к API.
     * Используется при взаимодействии с фронтендом, размещённым на другом домене.
     *
     * @return экземпляр {@link CorsFilter}, применяемый ко всем эндпоинтам.
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}