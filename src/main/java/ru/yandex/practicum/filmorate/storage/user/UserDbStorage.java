package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<User> findAll() {
        log.info("Получение списка всех пользователей из БД");
        final String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    @Override
    public Optional<User> findById(final Long id) {
        log.info("Получение пользователя с id={} из БД", id);
        final String sql = "SELECT * FROM users WHERE id = ?";
        final List<User> users = jdbcTemplate.query(sql, this::mapRowToUser, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public User create(final User user) {
        log.info("Создание пользователя с login={} в БД", user.getLogin());
        final String sql = "INSERT INTO users (name, login, email, birthday) VALUES (?, ?, ?, ?)";
        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            final PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setString(1, user.getName());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getEmail());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        log.info("Пользователь с id={} успешно создан", user.getId());
        return user;
    }

    @Override
    public User update(final User user) {
        log.info("Обновление пользователя с id={} в БД", user.getId());
        final String sql = "UPDATE users SET name = ?, login = ?, email = ?, birthday = ? WHERE id = ?";

        final int rows = jdbcTemplate.update(sql,
                user.getName(), user.getLogin(), user.getEmail(), user.getBirthday(), user.getId());

        if (rows == 0) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }

        log.info("Пользователь с id={} успешно обновлён", user.getId());
        return user;
    }

    @Override
    public void delete(final Long id) {
        log.info("Удаление пользователя с id={} из БД", id);
        final String sql = "DELETE FROM users WHERE id = ?";
        final int rows = jdbcTemplate.update(sql, id);

        if (rows == 0) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }

        log.info("Пользователь с id={} успешно удалён", id);
    }

    private User mapRowToUser(final ResultSet rs, final int rowNum) throws SQLException {
        final User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setLogin(rs.getString("login"));
        user.setEmail(rs.getString("email"));
        user.setBirthday(rs.getObject("birthday", java.time.LocalDate.class));
        return user;
    }
}
