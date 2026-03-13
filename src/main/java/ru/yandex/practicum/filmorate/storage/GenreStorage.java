package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreStorage {
    /**
     * Получить все жанры.
     */
    List<Genre> findAllGenres();

    /**
     * Найти жанр по ID.
     */
    Optional<Genre> findGenreById(Long id);

    /**
     * Найти все жанры для конкретного фильма по его ID.
     * Использует JOIN с таблицей film_genres, чтобы избежать запросов в цикле.
     * @param filmId ID фильма
     * @return Список жанров фильма
     */
    List<Genre> findGenresByFilmId(Long filmId);

    /**
     * Устаревший метод массового обогащения.
     * Теперь используется findGenresByFilmId внутри сервиса для каждого фильма.
     * Можно оставить пустым или удалить, если он нигде не вызывается.
     */
    default void findAllGenresByFilm(List<Film> films) {
        // Логика перенесена в FilmService.enrichFilmData через вызов findGenresByFilmId
    }
}
