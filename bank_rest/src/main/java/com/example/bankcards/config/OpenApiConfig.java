package com.example.bankcards.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI (Swagger) для документации REST API приложения Bank Cards.
 *
 * <p>Настройка включает:
 * <ul>
 *   <li>Информацию о приложении: название, описание, версия и контактные данные разработчика.</li>
 *   <li>Настройку схемы безопасности JWT Bearer, чтобы защищенные эндпоинты требовали токен.</li>
 *   <li>Добавление SecurityRequirement для автоматической интеграции с Swagger UI.</li>
 * </ul></p>
 *
 * <p>Swagger UI будет доступен по пути /swagger-ui.html, а JSON OpenAPI спецификация — по /api-docs.</p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Создает и настраивает Bean {@link OpenAPI} для Swagger документации.
     *
     * @return настроенный экземпляр {@link OpenAPI} с информацией о приложении и настройками безопасности JWT
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank Cards Management System API")
                        .description("REST API for managing bank cards, users, and transfers")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Sergei")
                                .email("sergei@example.com")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}