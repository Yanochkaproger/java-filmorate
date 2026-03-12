package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Хранилище фильмов в памяти приложения.
 */
@Component
public class InMemoryFilmStorage implements FilmStorage {

    /** Хранилище фильмов в памяти приложения. */
    private final Map<Long, Film> films = new HashMap<>();

    /** Счётчик для генерации идентификаторов. */
    private final AtomicLong nextId = new AtomicLong(1);

    /** Минимальная дата релиза (первый фильм в истории). */
    private static final LocalDate MIN_RELEASE_DATE =
            LocalDate.of(1895, 12, 28);

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Optional<Film> findById(final Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film create(final Film film) {
        validateFilm(film);
        film.setId(nextId.getAndIncrement());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(final Film film) {
        validateFilm(film);
        if (film.getId() == null
                || !films.containsKey(film.getId())) {
            throw new NotFoundException(
                    "Фильм с id = " + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void delete(final Long id) {
        if (id == null || !films.containsKey(id)) {
            throw new NotFoundException(
                    "Фильм с id = " + id + " не найден");
        }
        films.remove(id);
    }

    /**
     * Валидирует данные фильма.
     * @param film фильм для проверки
     */
    private void validateFilm(final Film film) {
        if (film.getName() == null
                || film.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Название фильма не может быть пустым");
        }
        if (film.getDescription() != null
                && film.getDescription().length() > 200) {
            throw new IllegalArgumentException(
                    "Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() != null
                && film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new IllegalArgumentException(
                    "Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() != null
                && film.getDuration() <= 0) {
            throw new IllegalArgumentException(
                    "Продолжительность фильма должна быть "
                            + "положительным числом");
        }
    }
}
