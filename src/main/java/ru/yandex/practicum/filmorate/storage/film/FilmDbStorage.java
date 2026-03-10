package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> findAll() {
        log.info("Получение списка всех фильмов из БД");
        final String sql = "SELECT * FROM films";
        final List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        for (final Film film : films) {
            film.setGenres(loadGenres(film.getId()));
            film.setMpa(loadMpaRating(film.getMpaId()));
        }
        return films;
    }

    @Override
    public Optional<Film> findById(final Long id) {
        log.info("Получение фильма с id={} из БД", id);
        final String sql = "SELECT * FROM films WHERE id = ?";
        final List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        final Film film = films.get(0);
        film.setGenres(loadGenres(film.getId()));
        film.setMpa(loadMpaRating(film.getMpaId()));
        return Optional.of(film);
    }

    @Override
    public Film create(final Film film) {
        log.info("Создание фильма: {} в БД", film.getName());
        final String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            final PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getMpaId());
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        updateFilmGenres(film.getId(), film.getGenreIds());
        log.info("Фильм с id={} успешно создан", film.getId());
        return film;
    }

    @Override
    public Film update(final Film film) {
        log.info("Обновление фильма с id={} в БД", film.getId());
        final String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

        final int rows = jdbcTemplate.update(sql,
                film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpaId(), film.getId());

        if (rows == 0) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }

        updateFilmGenres(film.getId(), film.getGenreIds());
        log.info("Фильм с id={} успешно обновлён", film.getId());
        return film;
    }

    @Override
    public void delete(final Long id) {
        log.info("Удаление фильма с id={} из БД", id);
        final String sql = "DELETE FROM films WHERE id = ?";
        final int rows = jdbcTemplate.update(sql, id);

        if (rows == 0) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }

        log.info("Фильм с id={} успешно удалён", id);
    }

    @Override
    public List<Genre> findAllGenres() {
        log.info("Получение списка всех жанров из БД");
        final String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, this::mapRowToGenre);
    }

    @Override
    public Optional<Genre> findGenreById(final Long id) {
        log.info("Получение жанра с id={} из БД", id);
        final String sql = "SELECT * FROM genres WHERE id = ?";
        final List<Genre> genres = jdbcTemplate.query(sql, this::mapRowToGenre, id);
        return genres.isEmpty() ? Optional.empty() : Optional.of(genres.get(0));
    }

    @Override
    public List<MpaRating> findAllMpaRatings() {
        log.info("Получение списка всех рейтингов MPA из БД");
        final String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, this::mapRowToMpaRating);
    }

    @Override
    public Optional<MpaRating> findMpaRatingById(final Long id) {
        log.info("Получение рейтинга MPA с id={} из БД", id);
        final String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        final List<MpaRating> ratings = jdbcTemplate.query(sql, this::mapRowToMpaRating, id);
        return ratings.isEmpty() ? Optional.empty() : Optional.of(ratings.get(0));
    }

    private Set<Genre> loadGenres(final Long filmId) {
        final String sql = "SELECT g.* FROM genres g JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ?";
        final List<Genre> genres = jdbcTemplate.query(sql, this::mapRowToGenre, filmId);
        return new HashSet<>(genres);
    }

    private MpaRating loadMpaRating(final Integer mpaId) {
        if (mpaId == null) {
            return null;
        }
        final String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        final List<MpaRating> ratings = jdbcTemplate.query(sql, this::mapRowToMpaRating, mpaId);
        return ratings.isEmpty() ? null : ratings.get(0);
    }

    private void updateFilmGenres(final Long filmId, final Set<Long> genreIds) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);
        if (genreIds != null && !genreIds.isEmpty()) {
            for (final Long genreId : genreIds) {
                jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", filmId, genreId);
            }
        }
    }

    private Film mapRowToFilm(final ResultSet rs, final int rowNum) throws SQLException {
        final Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getObject("release_date", java.time.LocalDate.class));
        film.setDuration(rs.getInt("duration"));
        film.setMpaId(rs.getObject("mpa_id", Integer.class));
        return film;
    }

    private Genre mapRowToGenre(final ResultSet rs, final int rowNum) throws SQLException {
        return new Genre(rs.getLong("id"), rs.getString("name"));
    }

    private MpaRating mapRowToMpaRating(final ResultSet rs, final int rowNum) throws SQLException {
        return new MpaRating(rs.getLong("id"), rs.getString("name"));
    }
}

