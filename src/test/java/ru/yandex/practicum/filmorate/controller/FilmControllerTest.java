package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для валидации фильмов.
 */
class FilmControllerTest {

    private FilmController filmController;
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        final InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        final InMemoryUserStorage userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage);
        filmController = new FilmController(filmService);
    }

    @Test
    void createFilmWithValidData() {
        Film film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        Film created = filmController.create(film);

        assertNotNull(created.getId());
        assertEquals("Тестовый фильм", created.getName());
    }

    @Test
    void createFilmWithEmptyName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> filmController.create(film)
        );

        assertEquals("Название фильма не может быть пустым",
                exception.getMessage());
    }

    @Test
    void createFilmWithNullName() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> filmController.create(film)
        );

        assertEquals("Название фильма не может быть пустым",
                exception.getMessage());
    }

    @Test
    void createFilmWithDescriptionOver200Characters() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> filmController.create(film)
        );

        assertEquals("Максимальная длина описания — 200 символов",
                exception.getMessage());
    }

    @Test
    void createFilmWithDescriptionExactly200Characters() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("a".repeat(200));
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        Film created = filmController.create(film);

        assertNotNull(created.getId());
    }

    @Test
    void createFilmWithReleaseDateBefore1895() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> filmController.create(film)
        );

        assertEquals("Дата релиза — не раньше 28 декабря 1895 года",
                exception.getMessage());
    }

    @Test
    void createFilmWithReleaseDateOn1895() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(120);

        Film created = filmController.create(film);

        assertNotNull(created.getId());
    }

    @Test
    void createFilmWithZeroDuration() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> filmController.create(film)
        );

        assertEquals("Продолжительность фильма должна быть "
                + "положительным числом", exception.getMessage());
    }

    @Test
    void createFilmWithNegativeDuration() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(-10);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> filmController.create(film)
        );

        assertEquals("Продолжительность фильма должна быть "
                + "положительным числом", exception.getMessage());
    }

    @Test
    void updateFilmWithValidData() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        Film created = filmController.create(film);
        created.setDescription("Обновлённое описание");

        Film updated = filmController.update(created);

        assertEquals("Обновлённое описание", updated.getDescription());
    }

    @Test
    void updateFilmWithEmptyName() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        Film created = filmController.create(film);
        created.setName("");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> filmController.update(created)
        );

        assertEquals("Название фильма не может быть пустым",
                exception.getMessage());
    }

    @Test
    void updateFilmWithNotFound() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.update(film)
        );

        assertEquals("Фильм с id = 999 не найден",
                exception.getMessage());
    }

    @Test
    void updateFilmWithNullId() {
        Film film = new Film();
        film.setId(null);
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.update(film)
        );

        assertEquals("Фильм с id = null не найден",
                exception.getMessage());
    }
}
