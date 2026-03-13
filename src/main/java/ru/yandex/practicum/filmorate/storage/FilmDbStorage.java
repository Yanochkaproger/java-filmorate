package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

        // Используем KeyHolder для получения сгенерированного ID (работает в H2 и PostgreSQL)
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            // ИСПРАВЛЕНИЕ: Явно указываем java.sql.Date, чтобы убрать неоднозначность
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());

            if (film.getMpa() != null && film.getMpa().getId() != null) {
                ps.setLong(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            return ps;
        }, keyHolder);

        // Получаем ID из ключа
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new RuntimeException("Не удалось получить сгенерированный ID для фильма");
        }

        Long filmId = key.longValue();
        film.setId(filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(filmId, film.getGenres());
        }

        return findById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден после создания"));
    }

    @Override
    public Film update(Film film) {
        if (!findById(film.getId()).isPresent()) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());

        deleteGenres(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(film.getId(), film.getGenres());
        }

        return findById(film.getId()).orElseThrow(() -> new NotFoundException("Фильм не найден после обновления"));
    }

    @Override
    public List<Film> findAllFilms() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        loadGenresForFilms(films);
        loadMpaForFilms(films);
        return films;
    }

    @Override
    public List<Film> findPopular(int count) {
        String sql = """
            SELECT f.*, COUNT(fl.user_id) as likes_count
            FROM films f
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            GROUP BY f.id
            ORDER BY likes_count DESC, f.id ASC
            LIMIT ?
        """;
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, count);
        loadGenresForFilms(films);
        loadMpaForFilms(films);
        return films;
    }

    @Override
    public Optional<Film> findFilmById(Long id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, id);
        if (!rs.next()) {
            return Optional.empty();
        }
        Film film = mapRowToFilmRs(rs);
        loadGenresForFilms(List.of(film));
        loadMpaForFilms(List.of(film));
        return Optional.of(film);
    }

    // Внутренний метод для поиска по ID (используется в create/update)
    private Optional<Film> findById(Long id) {
        return findFilmById(id);
    }

    private void saveGenres(Long filmId, List<Genre> genres) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre g : genres) {
            jdbcTemplate.update(sql, filmId, g.getId());
        }
    }

    private void deleteGenres(Long filmId) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    private void loadGenresForFilms(List<Film> films) {
        if (films.isEmpty()) return;
        List<Long> ids = films.stream().map(Film::getId).collect(Collectors.toList());
        String inSql = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT fg.film_id, g.id as genre_id, g.name as genre_name " +
                "FROM film_genres fg JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + inSql + ")";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, ids.toArray());
        Map<Long, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getId, f -> f));

        for (Map<String, Object> row : rows) {
            Long fid = ((Number) row.get("film_id")).longValue();
            Genre genre = new Genre(((Number) row.get("genre_id")).longValue(), (String) row.get("genre_name"));
            filmMap.get(fid).getGenres().add(genre);
        }
    }

    private void loadMpaForFilms(List<Film> films) {
        if (films.isEmpty()) return;
        List<Long> ids = films.stream()
                .filter(f -> f.getMpa() != null && f.getMpa().getId() != null)
                .map(f -> f.getMpa().getId())
                .distinct()
                .collect(Collectors.toList());

        if (ids.isEmpty()) return;

        String inSql = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id, name FROM mpa_ratings WHERE id IN (" + inSql + ")";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, ids.toArray());
        Map<Long, Mpa> mpaMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            mpaMap.put(((Number) row.get("id")).longValue(), new Mpa(((Number) row.get("id")).longValue(), (String) row.get("name")));
        }

        for (Film f : films) {
            if (f.getMpa() != null && mpaMap.containsKey(f.getMpa().getId())) {
                f.setMpa(mpaMap.get(f.getMpa().getId()));
            }
        }
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        if (rs.getObject("mpa_id") != null) {
            Mpa mpa = new Mpa();
            mpa.setId((long) rs.getInt("mpa_id"));
            film.setMpa(mpa);
        } else {
            film.setMpa(null);
        }
        film.setGenres(new ArrayList<>());
        return film;
    }

    private Film mapRowToFilmRs(SqlRowSet rs) {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        if (rs.getObject("mpa_id") != null) {
            Mpa mpa = new Mpa();
            mpa.setId((long) rs.getInt("mpa_id"));
            film.setMpa(mpa);
        } else {
            film.setMpa(null);
        }
        film.setGenres(new ArrayList<>());
        return film;
    }
}
