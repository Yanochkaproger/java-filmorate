package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (name, login, email, birthday) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getName(), user.getLogin(), user.getEmail(), user.getBirthday());

        // Получаем созданного пользователя по email
        return findUserByEmail(user.getEmail()).orElseThrow(() -> new NotFoundException("Не удалось создать пользователя"));
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET name = ?, login = ?, email = ?, birthday = ? WHERE id = ?";
        int rows = jdbcTemplate.update(sql, user.getName(), user.getLogin(), user.getEmail(), user.getBirthday(), user.getId());
        if (rows == 0) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        return findUserById(user.getId()).orElseThrow(() -> new NotFoundException("Пользователь не найден после обновления"));
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    @Override
    public Optional<User> findUserById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, id);
        if (!rs.next()) {
            return Optional.empty();
        }
        return Optional.of(mapRowToUserRs(rs));
    }

    private Optional<User> findUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, email);
        if (!rs.next()) {
            return Optional.empty();
        }
        return Optional.of(mapRowToUserRs(rs));
    }

    // Маппер для query (ResultSet) - используем билдер
    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        LocalDate birthday = rs.getDate("birthday") != null ? rs.getDate("birthday").toLocalDate() : null;

        return User.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .login(rs.getString("login"))
                .email(rs.getString("email"))
                .birthday(birthday)
                .build();
    }

    // Маппер для queryForRowSet (SqlRowSet) - используем билдер
    private User mapRowToUserRs(SqlRowSet rs) {
        LocalDate birthday = rs.getDate("birthday") != null ? rs.getDate("birthday").toLocalDate() : null;

        return User.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .login(rs.getString("login"))
                .email(rs.getString("email"))
                .birthday(birthday)
                .build();
    }
}
