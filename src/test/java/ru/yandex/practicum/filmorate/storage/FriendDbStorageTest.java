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

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({FriendDbStorage.class, UserDbStorage.class})
class FriendDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FriendDbStorage friendStorage;

    @Autowired
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");

        // Создаем тестовых пользователей
        User u1 = User.builder().name("User 1").login("u1").email("u1@test.ru").birthday(LocalDate.of(1990,1,1)).build();
        User u2 = User.builder().name("User 2").login("u2").email("u2@test.ru").birthday(LocalDate.of(1991,2,2)).build();
        User u3 = User.builder().name("User 3").login("u3").email("u3@test.ru").birthday(LocalDate.of(1992,3,3)).build();
        userStorage.create(u1);
        userStorage.create(u2);
        userStorage.create(u3);
    }

    @Test
    void testAddFriend() {
        friendStorage.addFriend(1L, 2L);

        List<User> friendsOf1 = friendStorage.findAllFriends(1L);
        assertThat(friendsOf1).hasSize(1);
        assertThat(friendsOf1.get(0).getId()).isEqualTo(2L);
    }

    @Test
    void testRemoveFriend() {
        friendStorage.addFriend(1L, 2L);
        friendStorage.removeFriend(1L, 2L);

        List<User> friendsOf1 = friendStorage.findAllFriends(1L);
        assertThat(friendsOf1).isEmpty();
    }

    @Test
    void testFindCommonFriends() {
        // Пользователь 3 является общим другом для 1 и 2
        friendStorage.addFriend(1L, 3L);
        friendStorage.addFriend(2L, 3L);
        // Также добавим уникальных друзей
        friendStorage.addFriend(1L, 2L);

        List<User> common = friendStorage.findCommonFriends(1L, 2L);

        assertThat(common).hasSize(1);
        assertThat(common.get(0).getId()).isEqualTo(3L);
    }

    @Test
    void testFindAllFriendsEmpty() {
        List<User> friends = friendStorage.findAllFriends(1L);
        assertThat(friends).isEmpty();
    }
}
