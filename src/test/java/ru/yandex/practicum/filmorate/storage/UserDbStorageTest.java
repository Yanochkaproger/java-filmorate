package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void testCreateUser() {
        User user = new User();
        user.setName("Иван Иванов");
        user.setLogin("ivan");
        user.setEmail("ivan@test.ru");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getLogin()).isEqualTo("ivan");
        log.info("User created with id: {}", created.getId());
    }

    @Test
    void testFindUserById() {
        User user = new User();
        user.setName("Иван Иванов");
        user.setLogin("ivan");
        user.setEmail("ivan@test.ru");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);
        Optional<User> found = userStorage.findById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u).hasFieldOrPropertyWithValue("id", created.getId())
                );
    }

    @Test
    void testFindAllUsers() {
        User user1 = new User();
        user1.setName("Иван");
        user1.setLogin("ivan");
        user1.setEmail("ivan@test.ru");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setName("Петр");
        user2.setLogin("petr");
        user2.setEmail("petr@test.ru");
        user2.setBirthday(LocalDate.of(1985, 5, 15));

        userStorage.create(user1);
        userStorage.create(user2);

        assertThat(userStorage.findAll()).hasSize(2);
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setName("Иван");
        user.setLogin("ivan");
        user.setEmail("ivan@test.ru");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);
        created.setName("Иван Обновлённый");

        User updated = userStorage.update(created);

        assertThat(updated.getName()).isEqualTo("Иван Обновлённый");
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setName("Иван");
        user.setLogin("ivan");
        user.setEmail("ivan@test.ru");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);
        userStorage.delete(created.getId());

        Optional<User> found = userStorage.findById(created.getId());
        assertThat(found).isEmpty();
    }
}
