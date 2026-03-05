package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.ErrorResponse;

/**
 * Обработчик ошибок контроллера.
 */
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
        return new ErrorResponse(exception.getMessage());
    }

    /**
     * Обработчик любого другого исключения.
     * @param exception исключение
     * @return ответ с ошибкой
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(
            final Throwable exception) {
        return new ErrorResponse("Произошла непредвиденная ошибка.");
    }
}
