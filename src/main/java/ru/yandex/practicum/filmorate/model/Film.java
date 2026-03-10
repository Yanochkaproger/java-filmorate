package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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

    /** Множество жанров фильма. */
    private Set<Genre> genres = new HashSet<>();

    /** Возрастной рейтинг фильма (MPA). */
    private MpaRating mpa;
}
