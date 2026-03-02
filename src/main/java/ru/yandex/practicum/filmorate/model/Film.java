package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import java.time.LocalDate;

/**
 * Модель фильма.
 */
@Data
public class Film {
    /** Уникальный идентификатор фильма. */
    private Long id;
    /** Название фильма. */
    private String name;
    /** Описание фильма. */
    private String description;
    /** Дата релиза фильма. */
    private LocalDate releaseDate;
    /** Продолжительность фильма в минутах. */
    private Integer duration;
}
