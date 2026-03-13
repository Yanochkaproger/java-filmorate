package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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

    @Override
    public void findAllGenresByFilm(List<Film> films) {
        // Этот метод теперь не используется в основной логике, оставляем пустым
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
