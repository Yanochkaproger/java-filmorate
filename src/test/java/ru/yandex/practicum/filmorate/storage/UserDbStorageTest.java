package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(UserDbStorage.class)
class UserDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        // Очищаем таблицу перед каждым тестом
        jdbcTemplate.update("DELETE FROM users");
        // Сбрасываем автоинкремент ID (для H2)
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void testCreate() {
        User user = User.builder()
                .name("Test User")
                .login("test_login")
                .email("test@mail.ru")
                .birthday(LocalDate.of(1990, 5, 5))
                .build();

        User created = userStorage.create(user);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getLogin()).isEqualTo("test_login");
        assertThat(created.getEmail()).isEqualTo("test@mail.ru");
    }

    @Test
    void testFindUserById() {
        // Создаем пользователя
        User user = User.builder()
                .name("Find Me")
                .login("find_login")
                .email("find@mail.ru")
                .birthday(LocalDate.of(1995, 1, 1))
                .build();
        userStorage.create(user);

        Optional<User> found = userStorage.findUserById(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Find Me");
        assertThat(found.get().getId()).isEqualTo(1L);
    }

    @Test
    void testFindUserByIdNotFound() {
        Optional<User> found = userStorage.findUserById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void testUpdate() {
        User user = User.builder()
                .name("Old Name")
                .login("old_login")
                .email("old@mail.ru")
                .birthday(LocalDate.of(1980, 1, 1))
                .build();
        User created = userStorage.create(user);

        created.setName("Updated Name");
        created.setLogin("new_login");

        User updated = userStorage.update(created);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getLogin()).isEqualTo("new_login");

        // Проверка в БД
        Optional<User> fromDb = userStorage.findUserById(created.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getName()).isEqualTo("Updated Name");
    }

    @Test
    void testFindAll() {
        User user1 = User.builder().name("User 1").login("login1").email("mail1@test.ru").birthday(LocalDate.of(2000, 1, 1)).build();
        User user2 = User.builder().name("User 2").login("login2").email("mail2@test.ru").birthday(LocalDate.of(2001, 2, 2)).build();

        userStorage.create(user1);
        userStorage.create(user2);

        List<User> users = userStorage.findAll();

        assertThat(users).hasSize(2);
        assertThat(users).extracting("login").containsExactlyInAnyOrder("login1", "login2");
    }
}
