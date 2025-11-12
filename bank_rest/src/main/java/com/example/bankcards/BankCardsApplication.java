package com.example.bankcards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс запуска Spring Boot приложения для управления банковскими картами.
 * <p>
 * Аннотация {@link SpringBootApplication} объединяет:
 * <ul>
 *     <li>{@link org.springframework.context.annotation.Configuration}</li>
 *     <li>{@link org.springframework.boot.autoconfigure.EnableAutoConfiguration}</li>
 *     <li>{@link org.springframework.context.annotation.ComponentScan}</li>
 * </ul>
 * <p>
 * Запускает встроенный сервер и инициализирует контекст приложения Spring.
 */
@SpringBootApplication
public class BankCardsApplication {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(BankCardsApplication.class, args);
    }
}