package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель жанра фильма.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Genre {

    /** Уникальный идентификатор жанра. */
    private Long id;

    /** Название жанра. */
    private String name;
}

