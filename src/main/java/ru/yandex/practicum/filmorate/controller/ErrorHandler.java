package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.ErrorResponse;

/**
 * Обработчик ошибок контроллера.
 */
@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    /**
     * Обработчик исключения NotFoundException.
     * @param exception исключение
     * @return ответ с ошибкой
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(
            final NotFoundException exception) {
        log.warn("Ресурс не найден: {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    /**
     * Обработчик исключения IllegalArgumentException.
     * @param exception исключение
     * @return ответ с ошибкой
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(
            final IllegalArgumentException exception) {
        log.warn("Некорректные данные запроса: {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    /**
     * Обработчик любого другого исключения.
     * @param exception исключение
     * @return ответ с ошибкой
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(
            final Exception exception) {
        log.error("Произошла непредвиденная ошибка: {}",
                exception.getMessage(), exception);
        return new ErrorResponse("Произошла непредвиденная ошибка.");
    }
}
