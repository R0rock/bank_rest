package com.example.bankcards.dto;

import lombok.Data;

/**
 * Универсальный DTO для ответа REST API.
 *
 * <p>Содержит статус выполнения операции, сообщение и опциональные данные.</p>
 *
 * <p>Используется как обертка для успешных и ошибочных ответов API.</p>
 */
@Data
public class ApiResponse {

    /** Статус выполнения операции: true - успешно, false - ошибка */
    private boolean success;

    /** Сообщение с описанием результата операции */
    private String message;

    /** Данные, возвращаемые в ответе (может быть null) */
    private Object data;

    /**
     * Конструктор для ответа без данных.
     *
     * @param success статус выполнения операции
     * @param message сообщение о результате
     */
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Конструктор для ответа с данными.
     *
     * @param success статус выполнения операции
     * @param message сообщение о результате
     * @param data данные ответа
     */
    public ApiResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * Создает успешный ответ без данных.
     *
     * @param message сообщение о результате
     * @return объект {@link ApiResponse} с success=true
     */
    public static ApiResponse success(String message) {
        return new ApiResponse(true, message);
    }

    /**
     * Создает успешный ответ с данными.
     *
     * @param message сообщение о результате
     * @param data данные ответа
     * @return объект {@link ApiResponse} с success=true
     */
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(true, message, data);
    }

    /**
     * Создает ответ с ошибкой без данных.
     *
     * @param message сообщение об ошибке
     * @return объект {@link ApiResponse} с success=false
     */
    public static ApiResponse error(String message) {
        return new ApiResponse(false, message);
    }

    /**
     * Создает ответ с ошибкой с данными.
     *
     * @param message сообщение об ошибке
     * @param data дополнительные данные об ошибке
     * @return объект {@link ApiResponse} с success=false
     */
    public static ApiResponse error(String message, Object data) {
        return new ApiResponse(false, message, data);
    }
}