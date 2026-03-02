package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для управления фильмами.
 */
@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    /** Хранилище фильмов в памяти приложения. */
    private final Map<Long, Film> films = new HashMap<>();

    /** Счётчик для генерации идентификаторов. */
    private long nextId = 1;

    /** Минимальная дата релиза (первый фильм в истории). */
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    /**
     * Возвращает все фильмы.
     * @return коллекция фильмов
     */
    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получение списка всех фильмов");
        return films.values();
    }

    /**
     * Добавляет новый фильм.
     * @param film данные фильма из тела запроса
     * @return созданный фильм с установленным id
     */
    @PostMapping
    public Film create(final @RequestBody Film film) {
        log.info("Попытка добавления фильма: {}", film.getName());
        validateFilm(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм '{}' успешно добавлен с id={}", film.getName(), film.getId());
        return film;
    }

    /**
     * Обновляет существующий фильм.
     * @param film данные для обновления
     * @return обновлённый фильм
     */
    @SuppressWarnings("unused")
    @PutMapping
    public Film update(final @RequestBody Film film) {
        log.info("Попытка обновления фильма с id={}", film.getId());
        validateFilm(film);
        if (film.getId() != null && films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм с id={} успешно обновлён", film.getId());
        } else {
            log.warn("Фильм с id={} не найден для обновления", film.getId());
        }
        return film;
    }

    /**
     * Валидирует данные фильма.
     * @param film фильм для проверки
     */
    private void validateFilm(final Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации: название фильма не может быть пустым");
            throw new ValidationException(
                    "Название фильма не может быть пустым");
        }
        if (film.getDescription() != null
                && film.getDescription().length() > 200) {
            log.error("Ошибка валидации: максимальная длина описания — 200 символов");
            throw new ValidationException(
                    "Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() != null
                && film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Ошибка валидации: дата релиза раньше 28 декабря 1895 года");
            throw new ValidationException(
                    "Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() != null && film.getDuration() <= 0) {
            log.error("Ошибка валидации: продолжительность должна быть положительным числом");
            throw new ValidationException(
                    "Продолжительность фильма должна быть положительным числом");
        }
        log.debug("Валидация фильма прошла успешно");
    }
}
