package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> findAllMpa() {
        String sql = "SELECT id, name FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, this::mapRowToMpa);
    }

    @Override
    public Optional<Mpa> findMpaById(Long id) {
        String sql = "SELECT id, name FROM mpa_ratings WHERE id = ?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, id);
        if (!rs.next()) {
            return Optional.empty();
        }
        return Optional.of(mapRowToMpaRs(rs));
    }

    // Маппер для query (работает с ResultSet)
    private Mpa mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa((long) rs.getInt("id"), rs.getString("name"));
    }

    // Маппер для queryForRowSet (работает с SqlRowSet)
    private Mpa mapRowToMpaRs(SqlRowSet rs) {
        return new Mpa((long) rs.getInt("id"), rs.getString("name"));
    }
}


