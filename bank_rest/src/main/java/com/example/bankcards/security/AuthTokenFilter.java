package com.example.bankcards.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Фильтр аутентификации JWT, выполняющий проверку токена для каждого HTTP-запроса.
 * <p>
 * Расширяет {@link OncePerRequestFilter}, чтобы обрабатывать каждый запрос один раз.
 * Проверяет наличие и валидность JWT-токена, извлекает пользователя и устанавливает
 * {@link SecurityContextHolder} для дальнейшей авторизации.
 */
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    /**
     * Основной метод фильтра, который проверяет наличие JWT в заголовке Authorization,
     * валидирует его, загружает пользователя и устанавливает его в контекст безопасности.
     *
     * @param request     HTTP-запрос
     * @param response    HTTP-ответ
     * @param filterChain цепочка фильтров
     * @throws ServletException если ошибка фильтрации запроса
     * @throws IOException      если ошибка ввода-вывода
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Извлекает JWT-токен из заголовка Authorization HTTP-запроса.
     * <p>
     * Ожидается формат заголовка: "Authorization: Bearer <token>".
     *
     * @param request HTTP-запрос
     * @return JWT-токен, если он присутствует и корректный, иначе {@code null}
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}