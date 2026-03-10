package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();
    Optional<Film> findById(Long id);
    Film create(Film film);
    Film update(Film film);
    void delete(Long id);
    List<Genre> findAllGenres();
    Optional<Genre> findGenreById(Long id);
    List<MpaRating> findAllMpaRatings();
    Optional<MpaRating> findMpaRatingById(Long id);
}
