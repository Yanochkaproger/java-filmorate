package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendStorage friendStorage;

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        validate(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        validate(user);
        userStorage.findUserById(user.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + user.getId() + " не найден"));
        return userStorage.update(user);
    }

    public User findUserById(Long id) {
        return userStorage.findUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        // Проверка существования обоих пользователей
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        userStorage.findUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + friendId + " не найден"));

        friendStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        userStorage.findUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + friendId + " не найден"));

        friendStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        return friendStorage.findAllFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        userStorage.findUserById(otherId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + otherId + " не найден"));

        return friendStorage.findCommonFriends(userId, otherId);
    }

    private void validate(User user) {
        // Ваша логика валидации, если она нужна здесь вручную.
        // Обычно это делается через @Valid в контроллере.
    }
}
