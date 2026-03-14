package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> findAllGenres() {
        String sql = "SELECT id, name FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, this::mapRowToGenre);
    }

    @Override
    public Optional<Genre> findGenreById(Long id) {
        String sql = "SELECT id, name FROM genres WHERE id = ?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, id);
        if (!rs.next()) {
            return Optional.empty();
        }
        return Optional.of(mapRowToGenreRs(rs));
    }

    @Override
    public List<Genre> findGenresByFilmId(Long filmId) {
        String sql = """
            SELECT g.id, g.name
            FROM genres g
            JOIN film_genres fg ON g.id = fg.genre_id
            WHERE fg.film_id = ?
            ORDER BY g.id
        """;
        return jdbcTemplate.query(sql, this::mapRowToGenre, filmId);
    }

    /**
     * Загружает жанры для переданного списка фильмов ОДНИМ запросом к БД.
     * Использует оператор IN для выборки всех связей сразу, избегая циклов.
     */
    @Override
    public void findAllGenresByFilm(List<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }

        // 1. Собираем ID всех фильмов из списка
        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        // 2. Формируем динамический SQL с нужным количеством знаков вопроса (?)
        String inSql = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = "SELECT fg.film_id, g.id as genre_id, g.name as genre_name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + inSql + ")";

        // 3. Выполняем один запрос для всех фильмов сразу
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, filmIds.toArray());

        // 4. Создаем карту для быстрого доступа к объекту фильма по его ID
        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, f -> f));

        // 5. Распределяем полученные жанры по соответствующим фильмам
        for (Map<String, Object> row : rows) {
            Long fid = ((Number) row.get("film_id")).longValue();
            Genre genre = new Genre(
                    ((Number) row.get("genre_id")).longValue(),
                    (String) row.get("genre_name")
            );

            Film film = filmMap.get(fid);
            if (film != null) {
                if (film.getGenres() == null) {
                    film.setGenres(new ArrayList<>());
                }
                film.getGenres().add(genre);
            }
        }
    }

    // Маппер для query (ResultSet)
    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre((long) rs.getInt("id"), rs.getString("name"));
    }

    // Маппер для queryForRowSet (SqlRowSet)
    private Genre mapRowToGenreRs(SqlRowSet rs) {
        return new Genre((long) rs.getInt("id"), rs.getString("name"));
    }
}
