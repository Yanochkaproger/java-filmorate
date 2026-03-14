package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({FilmDbStorage.class, MpaDbStorage.class, GenreDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private MpaDbStorage mpaStorage;

    @Autowired
    private GenreDbStorage genreStorage;

    @BeforeEach
    void setUp() {
        // Очистка в порядке, обратном созданию (из-за внешних ключей)
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void testCreateFilm() {
        Mpa mpa = mpaStorage.findMpaById(1L).orElseThrow();
        Film film = Film.builder()
                .name("Test Film")
                .description("Description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(100)
                .mpa(mpa)
                .build();

        Film created = filmStorage.create(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Test Film");
        assertThat(created.getMpa()).isNotNull();
        assertThat(created.getMpa().getId()).isEqualTo(1L);
    }

    @Test
    void testCreateFilmWithGenres() {
        Mpa mpa = mpaStorage.findMpaById(1L).orElseThrow();
        List<Genre> genres = List.of(
                new Genre(1L, "Комедия"),
                new Genre(2L, "Драма")
        );

        Film film = Film.builder()
                .name("Film with Genres")
                .description("Desc")
                .releaseDate(LocalDate.of(2021, 5, 5))
                .duration(120)
                .mpa(mpa)
                .genres(genres)
                .build();

        Film created = filmStorage.create(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getGenres()).hasSize(2);

        // Проверка, что жанры сохранились в БД
        Optional<Film> fromDb = filmStorage.findFilmById(created.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getGenres()).hasSize(2);
        assertThat(fromDb.get().getGenres()).extracting("id").containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void testFindFilmById() {
        Mpa mpa = mpaStorage.findMpaById(3L).orElseThrow();
        Film film = Film.builder()
                .name("Find Me Film")
                .description("Desc")
                .releaseDate(LocalDate.of(2019, 1, 1))
                .duration(90)
                .mpa(mpa)
                .build();
        filmStorage.create(film);

        Optional<Film> found = filmStorage.findFilmById(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Find Me Film");
        assertThat(found.get().getMpa().getName()).isEqualTo("PG-13"); // Проверка подгрузки MPA
    }

    @Test
    void testFindFilmByIdNotFound() {
        Optional<Film> found = filmStorage.findFilmById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void testUpdateFilm() {
        Mpa mpa = mpaStorage.findMpaById(1L).orElseThrow();
        Film film = Film.builder()
                .name("Old Title")
                .description("Old Desc")
                .releaseDate(LocalDate.of(2010, 1, 1))
                .duration(80)
                .mpa(mpa)
                .build();
        Film created = filmStorage.create(film);

        created.setName("New Title");
        created.setDescription("New Desc");
        created.setMpa(mpaStorage.findMpaById(2L).orElseThrow());

        Film updated = filmStorage.update(created);

        assertThat(updated.getName()).isEqualTo("New Title");
        assertThat(updated.getMpa().getId()).isEqualTo(2L);
    }

    @Test
    void testFindPopular() {
        Mpa mpa = mpaStorage.findMpaById(1L).orElseThrow();

        Film film1 = new Film();
        film1.setName("Film 1"); film1.setDescription("D"); film1.setReleaseDate(LocalDate.of(2020,1,1)); film1.setDuration(100); film1.setMpa(mpa);

        Film film2 = new Film();
        film2.setName("Film 2"); film2.setDescription("D"); film2.setReleaseDate(LocalDate.of(2020,2,2)); film2.setDuration(100); film2.setMpa(mpa);

        Film f1 = filmStorage.create(film1);
        Film f2 = filmStorage.create(film2);

        // ИСПРАВЛЕНИЕ: Сначала создаем пользователей, чтобы их ID существовали в БД
        // Используем JdbcTemplate для прямой вставки, так как UserStorage может быть не внедрен в этот тест
        jdbcTemplate.update("INSERT INTO users (name, login, email, birthday) VALUES (?, ?, ?, ?)",
                "User 100", "login100", "mail100@test.ru", LocalDate.of(2000, 1, 1));
        jdbcTemplate.update("INSERT INTO users (name, login, email, birthday) VALUES (?, ?, ?, ?)",
                "User 101", "login101", "mail101@test.ru", LocalDate.of(2000, 1, 1));

        // Теперь добавляем лайки от существующих пользователей (ID 1 и 2, так как это первые записи после очистки)
        // Или можно получить их ID через запрос, но для простоты используем 1 и 2, если таблица users была очищена в setUp
        // Если users не чистится в этом тесте, лучше использовать конкретные ID созданных выше пользователей.
        // Самый надежный способ - выбрать ID только что созданных пользователей:
        Long userId1 = jdbcTemplate.queryForObject("SELECT id FROM users WHERE login = ?", Long.class, "login100");
        Long userId2 = jdbcTemplate.queryForObject("SELECT id FROM users WHERE login = ?", Long.class, "login101");

        jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)", f1.getId(), userId1);
        jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)", f1.getId(), userId2);
        jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)", f2.getId(), userId1);

        List<Film> popular = filmStorage.findPopular(2);

        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getId()).isEqualTo(f1.getId()); // У фильма 1 два лайка
        assertThat(popular.get(1).getId()).isEqualTo(f2.getId()); // У фильма 2 один лайк
    }

    @Test
    void testFindAllFilmsWithDetails() {
        Mpa mpa = mpaStorage.findMpaById(4L).orElseThrow();
        Film film = Film.builder()
                .name("All Films Test")
                .description("Desc")
                .releaseDate(LocalDate.of(2022, 1, 1))
                .duration(110)
                .mpa(mpa)
                .genres(List.of(new Genre(3L, "Мультфильм")))
                .build();
        filmStorage.create(film);

        List<Film> films = filmStorage.findAllFilms();

        assertThat(films).hasSize(1);
        Film retrieved = films.get(0);
        assertThat(retrieved.getMpa().getName()).isEqualTo("R");
        assertThat(retrieved.getGenres()).hasSize(1);
        assertThat(retrieved.getGenres().get(0).getName()).isEqualTo("Мультфильм");
    }
}

