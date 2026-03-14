package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LikeDbStorage implements LikeStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addLike(Long filmId, Long userId) {
        // Используем MERGE для H2 (или INSERT ... ON CONFLICT DO NOTHING для Postgres)
        // Это предотвратит дублирование лайка от одного пользователя
        String sql = "MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public int getLikeCount(Long filmId) {
        String sql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId);
        return count != null ? count : 0;
    }

    // Опционально: метод для получения всех лайков фильма (если понадобится для других задач)
    public java.util.Set<Long> getLikedUserIds(Long filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return new java.util.HashSet<>(jdbcTemplate.queryForList(sql, Long.class, filmId));
    }
}

