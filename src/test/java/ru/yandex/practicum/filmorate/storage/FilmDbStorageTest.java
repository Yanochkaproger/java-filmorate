package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@JdbcTest
@AutoConfigureTestDatabase
@Import(FilmDbStorage.class)  // ✅ ЭТА АННОТАЦИЯ ВАЖНА!
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    @Test
    void testCreateFilm() {
        Film film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        Film created = filmStorage.create(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Тестовый фильм");
    }

    @Test
    void testFindFilmById() {
        Film film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        Film created = filmStorage.create(film);
        Optional<Film> found = filmStorage.findById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("id", created.getId())
                );
    }

    @Test
    void testFindAllFilms() {
        Film film1 = new Film();
        film1.setName("Фильм 1");
        film1.setDescription("Описание 1");
        film1.setReleaseDate(LocalDate.of(2024, 1, 1));
        film1.setDuration(120);

        Film film2 = new Film();
        film2.setName("Фильм 2");
        film2.setDescription("Описание 2");
        film2.setReleaseDate(LocalDate.of(2024, 2, 1));
        film2.setDuration(100);

        filmStorage.create(film1);
        filmStorage.create(film2);

        assertThat(filmStorage.findAll()).hasSize(2);
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        Film created = filmStorage.create(film);
        created.setDescription("Обновлённое описание");

        Film updated = filmStorage.update(created);

        assertThat(updated.getDescription()).isEqualTo("Обновлённое описание");
    }

    @Test
    void testFindAllGenres() {
        assertThat(filmStorage.findAllGenres()).hasSize(6);
    }

    @Test
    void testFindAllMpaRatings() {
        assertThat(filmStorage.findAllMpaRatings()).hasSize(5);
    }
}
