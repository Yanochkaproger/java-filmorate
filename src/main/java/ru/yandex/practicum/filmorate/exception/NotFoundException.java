package ru.yandex.practicum.filmorate.exception;

/**
 * Исключение, выбрасываемое когда сущность не найдена.
 */
public class NotFoundException extends RuntimeException {
    /**
     * Конструктор исключения с сообщением.
     * @param message сообщение об ошибке
     */
    public NotFoundException(final String message) {
        super(message);
    }
}

