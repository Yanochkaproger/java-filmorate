package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель возрастного рейтинга фильма (MPA).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MpaRating {

    /** Уникальный идентификатор рейтинга. */
    private Long id;

    /** Название рейтинга (G, PG, PG-13, R, NC-17). */
    private String name;
}

