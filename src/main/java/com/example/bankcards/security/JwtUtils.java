package com.example.bankcards.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

/**
 * Утилита для работы с JWT-токенами.
 * <p>
 * Отвечает за генерацию, валидацию и извлечение информации из JWT.
 * Используется фильтром {@link AuthTokenFilter} и контроллерами аутентификации.
 */
@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    /**
     * Секретный ключ для подписи JWT, задается в application.properties.
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Время жизни токена в миллисекундах, задается в application.properties.
     */
    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Генерирует JWT-токен для аутентифицированного пользователя.
     *
     * @param authentication объект аутентификации Spring Security
     * @return сгенерированный JWT-токен
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Создает секретный ключ для подписи JWT из строки {@link #jwtSecret}.
     *
     * @return объект {@link Key} для подписи
     */
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Извлекает имя пользователя (логин) из JWT-токена.
     *
     * @param token JWT-токен
     * @return имя пользователя, указанное в токене
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Валидирует JWT-токен, проверяя подпись, срок действия и формат.
     *
     * @param authToken JWT-токен для проверки
     * @return {@code true}, если токен корректный и не истек; иначе {@code false}
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}