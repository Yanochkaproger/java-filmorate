package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Модель ответа с ошибкой.
 */
@Data
@AllArgsConstructor
public class ErrorResponse {

    /** Описание ошибки. */
    private final String error;
}
