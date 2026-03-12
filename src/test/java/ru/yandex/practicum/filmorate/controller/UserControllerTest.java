package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для валидации пользователей.
 */
class UserControllerTest {

    private UserController userController;
    private UserService userService;

    @BeforeEach
    void setUp() {
        final InMemoryUserStorage userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        userController = new UserController(userService);
    }

    @Test
    void createUserWithValidData() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user123");
        user.setName("Иван Иванов");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.create(user);

        assertNotNull(created.getId());
        assertEquals("user@example.com", created.getEmail());
        assertEquals("Иван Иванов", created.getName());
    }

    @Test
    void createUserWithEmptyEmail() {
        User user = new User();
        user.setEmail("");
        user.setLogin("user123");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userController.create(user)
        );

        assertEquals("Электронная почта не может быть пустой",
                exception.getMessage());
    }

    @Test
    void createUserWithNullEmail() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("user123");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userController.create(user)
        );

        assertEquals("Электронная почта не может быть пустой",
                exception.getMessage());
    }

    @Test
    void createUserWithEmailWithoutAt() {
        User user = new User();
        user.setEmail("userexample.com");
        user.setLogin("user123");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userController.create(user)
        );

        assertEquals("Электронная почта должна содержать символ @",
                exception.getMessage());
    }

    @Test
    void createUserWithEmptyLogin() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userController.create(user)
        );

        assertEquals("Логин не может быть пустым",
                exception.getMessage());
    }

    @Test
    void createUserWithNullLogin() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin(null);
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userController.create(user)
        );

        assertEquals("Логин не может быть пустым",
                exception.getMessage());
    }

    @Test
    void createUserWithLoginContainingSpaces() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user 123");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userController.create(user)
        );

        assertEquals("Логин не может содержать пробелы",
                exception.getMessage());
    }

    @Test
    void createUserWithEmptyName() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user123");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.create(user);

        assertEquals("user123", created.getName());
    }

    @Test
    void createUserWithNullName() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user123");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.create(user);

        assertEquals("user123", created.getName());
    }

    @Test
    void createUserWithBirthdayInFuture() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user123");
        user.setName("Иван");
        user.setBirthday(LocalDate.now().plusDays(1));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userController.create(user)
        );

        assertEquals("Дата рождения не может быть в будущем",
                exception.getMessage());
    }

    @Test
    void createUserWithBirthdayToday() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user123");
        user.setName("Иван");
        user.setBirthday(LocalDate.now());

        User created = userController.create(user);

        assertNotNull(created.getId());
    }

    @Test
    void createUserWithNullBirthday() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user123");
        user.setName("Иван");
        user.setBirthday(null);

        User created = userController.create(user);

        assertNotNull(created.getId());
    }

    @Test
    void updateUserWithValidData() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user123");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.create(user);
        created.setName("Обновлённое имя");

        User updated = userController.update(created);

        assertEquals("Обновлённое имя", updated.getName());
    }

    @Test
    void updateUserWithEmptyEmail() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user123");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.create(user);
        created.setEmail("");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userController.update(created)
        );

        assertEquals("Электронная почта не может быть пустой",
                exception.getMessage());
    }

    @Test
    void updateUserWithLoginContainingSpaces() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user123");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.create(user);
        created.setLogin("user 123");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userController.update(created)
        );

        assertEquals("Логин не может содержать пробелы",
                exception.getMessage());
    }

    @Test
    void updateUserWithNotFound() {
        User user = new User();
        user.setId(999L);
        user.setEmail("user@example.com");
        user.setLogin("user123");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userController.update(user)
        );

        assertEquals("Пользователь с id = 999 не найден",
                exception.getMessage());
    }

    @Test
    void updateUserWithNullId() {
        User user = new User();
        user.setId(null);
        user.setEmail("user@example.com");
        user.setLogin("user123");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userController.update(user)
        );

        assertEquals("Пользователь с id = null не найден",
                exception.getMessage());
    }
}
