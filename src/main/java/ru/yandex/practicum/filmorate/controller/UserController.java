package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для управления пользователями.
 */
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    /** Хранилище пользователей в памяти приложения. */
    private final Map<Long, User> users = new HashMap<>();

    /** Счётчик для генерации идентификаторов. */
    private long nextId = 1;

    /**
     * Возвращает всех пользователей.
     * @return коллекция пользователей
     */
    @GetMapping
    public Collection<User> findAll() {
        log.info("Получение списка всех пользователей");
        return users.values();
    }

    /**
     * Создаёт нового пользователя.
     * @param user данные пользователя из тела запроса
     * @return созданный пользователь с установленным id
     */
    @PostMapping
    public User create(final @RequestBody User user) {
        log.info("Попытка создания пользователя с login={}", user.getLogin());
        validateUser(user);
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Пользователь '{}' успешно создан с id={}", user.getLogin(), user.getId());
        return user;
    }

    /**
     * Обновляет существующего пользователя.
     * @param user данные для обновления
     * @return обновлённый пользователь
     */
    @SuppressWarnings("unused")
    @PutMapping
    public User update(final @RequestBody User user) {
        log.info("Попытка обновления пользователя с id={}", user.getId());
        validateUser(user);
        if (user.getId() != null && users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            log.info("Пользователь с id={} успешно обновлён", user.getId());
        } else {
            log.warn("Пользователь с id={} не найден для обновления", user.getId());
        }
        return user;
    }

    /**
     * Валидирует данные пользователя.
     * @param user пользователь для проверки
     */
    private void validateUser(final User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("Ошибка валидации: электронная почта не может быть пустой");
            throw new ValidationException(
                    "Электронная почта не может быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            log.error("Ошибка валидации: электронная почта должна содержать символ @");
            throw new ValidationException(
                    "Электронная почта должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error("Ошибка валидации: логин не может быть пустым");
            throw new ValidationException(
                    "Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Ошибка валидации: логин не может содержать пробелы");
            throw new ValidationException(
                    "Логин не может содержать пробелы");
        }
        if (user.getBirthday() != null
                && user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка валидации: дата рождения не может быть в будущем");
            throw new ValidationException(
                    "Дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя пустое, установлено значение login={}", user.getLogin());
        }
        log.debug("Валидация пользователя прошла успешно");
    }
}

