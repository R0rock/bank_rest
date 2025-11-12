package com.example.bankcards.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурационный класс для настройки {@link ModelMapper}.
 *
 * <p>ModelMapper используется для автоматического отображения (mapping) между
 * объектами DTO и сущностями базы данных. Это упрощает преобразование данных
 * между слоями приложения без ручного копирования полей.</p>
 *
 * <p>Пример использования:
 * <pre>{@code
 * User user = userRepository.findById(1L);
 * UserProfileDto dto = modelMapper.map(user, UserProfileDto.class);
 * }</pre>
 * </p>
 */
@Configuration
public class ModelMapperConfig {

    /**
     * Создает и регистрирует {@link ModelMapper} как Spring Bean.
     *
     * @return новый экземпляр {@link ModelMapper}
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}