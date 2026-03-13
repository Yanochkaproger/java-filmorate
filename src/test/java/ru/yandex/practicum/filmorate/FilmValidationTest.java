package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {
    private Film film;
    private static Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    void validateFilmName() {
        film = new Film();
        film.setId(1L);
        film.setName(""); // Пустое имя для ошибки валидации
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2002, 2, 2));
        film.setDuration(100);
        film.setMpa(new Mpa(1L, "G"));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Введите название фильма.", violations.iterator().next().getMessage());
    }

    @Test
    void validateFilmDescription() {
        String longDescription = "Из-под покрова тьмы ночной,\n" +
                "Из чёрной ямы страшных мук\n" +
                "Благодарю я всех богов\n" +
                "За мой непокорённый дух.\n" +
                "\n" +
                "И я, попав в тиски беды,\n" +
                "Не дрогнул и не застонал,\n" +
                "И под ударами судьбы\n" +
                "Я ранен был, но не упал.\n" +
                "\n" +
                "Т"; // Длинное описание (>200 символов)

        film = new Film();
        film.setId(1L);
        film.setName("Film");
        film.setDescription(longDescription);
        film.setReleaseDate(LocalDate.of(2002, 2, 2));
        film.setDuration(100);
        film.setMpa(new Mpa(1L, "G"));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Слишком длинное описание.", violations.iterator().next().getMessage());
    }

    @Test
    void validateFilmReleaseDate() {
        film = new Film();
        film.setId(1L);
        film.setName("Film");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // Дата раньше допустимой
        film.setDuration(100);
        film.setMpa(new Mpa(1L, "G"));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Введите дату релиза не ранее 28 декабря 1895 года.",
                violations.iterator().next().getMessage());
    }

    @Test
    void validateFilmDuration() {
        film = new Film();
        film.setId(1L);
        film.setName("Film");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(1895, 12, 29));
        film.setDuration(-100); // Отрицательная длительность
        film.setMpa(new Mpa(1L, "G"));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Продолжительность фильма должна быть больше 0.",
                violations.iterator().next().getMessage());
    }
}
