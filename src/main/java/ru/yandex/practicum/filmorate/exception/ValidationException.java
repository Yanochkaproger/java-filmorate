package ru.yandex.practicum.filmorate.exception;

/**
 * Исключение, выбрасываемое при ошибке валидации данных.
 */
public class ValidationException extends RuntimeException {
    /**
     * Конструктор исключения с сообщением.
     * @param message сообщение об ошибке
     */
    public ValidationException(final String message) {
        super(message);
    }
}

