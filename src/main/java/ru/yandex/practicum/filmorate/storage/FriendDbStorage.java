package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FriendDbStorage implements FriendStorage {

    private final JdbcTemplate jdbcTemplate;
    // UserStorage здесь не нужен для запросов, так как мы делаем JOIN сразу в SQL

    @Override
    public void addFriend(Long userId, Long friendId) {
        String sql = "MERGE INTO friendship (user_id, friend_id) KEY (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> findAllFriends(Long userId) {
        String sql = """
            SELECT u.* 
            FROM friendship f
            JOIN users u ON f.friend_id = u.id
            WHERE f.user_id = ?
            ORDER BY u.id
        """;
        return jdbcTemplate.query(sql, this::mapRowToUser, userId);
    }

    @Override
    public List<User> findCommonFriends(Long userId, Long otherId) {
        String sql = """
            SELECT u.* 
            FROM users u
            JOIN friendship f1 ON u.id = f1.friend_id
            JOIN friendship f2 ON u.id = f2.friend_id
            WHERE f1.user_id = ? 
              AND f2.user_id = ?
            ORDER BY u.id
        """;
        return jdbcTemplate.query(sql, this::mapRowToUser, userId, otherId);
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
}



