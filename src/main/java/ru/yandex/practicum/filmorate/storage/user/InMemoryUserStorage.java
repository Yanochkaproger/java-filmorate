package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Хранилище пользователей в памяти приложения.
 */
@Component
public class InMemoryUserStorage implements UserStorage {

    /** Хранилище пользователей в памяти приложения. */
    private final Map<Long, User> users = new HashMap<>();

    /** Счётчик для генерации идентификаторов. */
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public Optional<User> findById(final Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User create(final User user) {
        validateUser(user);
        user.setId(nextId.getAndIncrement());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(final User user) {
        validateUser(user);
        if (user.getId() == null
                || !users.containsKey(user.getId())) {
            throw new NotFoundException(
                    "Пользователь с id = " + user.getId()
                            + " не найден");
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(final Long id) {
        if (id == null || !users.containsKey(id)) {
            throw new NotFoundException(
                    "Пользователь с id = " + id + " не найден");
        }
        users.remove(id);
    }

    /**
     * Валидирует данные пользователя.
     * @param user пользователь для проверки
     */
    private void validateUser(final User user) {
        if (user.getEmail() == null
                || user.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "Электронная почта не может быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            throw new IllegalArgumentException(
                    "Электронная почта должна содержать символ @");
        }
        if (user.getLogin() == null
                || user.getLogin().isBlank()) {
            throw new IllegalArgumentException(
                    "Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            throw new IllegalArgumentException(
                    "Логин не может содержать пробелы");
        }
        if (user.getBirthday() != null
                && user.getBirthday().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Дата рождения не может быть в будущем");
        }
        if (user.getName() == null
                || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
