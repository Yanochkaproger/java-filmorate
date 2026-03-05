package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис для управления пользователями и друзьями.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    /** Хранилище пользователей. */
    private final UserStorage userStorage;

    /** Хранилище дружеских связей: userId → набор friendId. */
    private final Set<Long> userIds = new HashSet<>();
    private final java.util.Map<Long, Set<Long>> friendsMap =
            new java.util.HashMap<>();

    /**
     * Возвращает всех пользователей.
     * @return список пользователей
     */
    public List<User> findAll() {
        log.info("Получение списка всех пользователей");
        return new ArrayList<>(userStorage.findAll());
    }

    /**
     * Возвращает пользователя по идентификатору.
     * @param id идентификатор пользователя
     * @return пользователь
     */
    public User findById(final Long id) {
        log.info("Получение пользователя с id={}", id);
        return userStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", id);
                    return new NotFoundException(
                            "Пользователь с id = " + id + " не найден");
                });
    }

    /**
     * Создаёт нового пользователя.
     * @param user данные пользователя
     * @return созданный пользователь
     */
    public User create(final User user) {
        log.info("Создание пользователя с login={}", user.getLogin());
        final User created = userStorage.create(user);
        friendsMap.put(created.getId(), new HashSet<>());
        log.info("Пользователь с id={} успешно создан", created.getId());
        return created;
    }

    /**
     * Обновляет существующего пользователя.
     * @param user данные для обновления
     * @return обновлённый пользователь
     */
    public User update(final User user) {
        log.info("Обновление пользователя с id={}", user.getId());
        final User updated = userStorage.update(user);
        log.info("Пользователь с id={} успешно обновлён", updated.getId());
        return updated;
    }

    /**
     * Добавляет пользователя в друзья другому пользователю.
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     */
    public void addFriend(final Long userId, final Long friendId) {
        log.info("Добавление в друзья: userId={}, friendId={}",
                userId, friendId);

        if (!userStorage.findById(userId).isPresent()) {
            throw new NotFoundException(
                    "Пользователь с id = " + userId + " не найден");
        }
        if (!userStorage.findById(friendId).isPresent()) {
            throw new NotFoundException(
                    "Пользователь с id = " + friendId + " не найден");
        }

        friendsMap.computeIfAbsent(userId, k -> new HashSet<>())
                .add(friendId);
        friendsMap.computeIfAbsent(friendId, k -> new HashSet<>())
                .add(userId);

        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    /**
     * Удаляет пользователя из друзей.
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     */
    public void removeFriend(final Long userId, final Long friendId) {
        log.info("Удаление из друзей: userId={}, friendId={}",
                userId, friendId);

        if (!userStorage.findById(userId).isPresent()) {
            throw new NotFoundException(
                    "Пользователь с id = " + userId + " не найден");
        }
        if (!userStorage.findById(friendId).isPresent()) {
            throw new NotFoundException(
                    "Пользователь с id = " + friendId + " не найден");
        }

        if (friendsMap.containsKey(userId)) {
            friendsMap.get(userId).remove(friendId);
        }
        if (friendsMap.containsKey(friendId)) {
            friendsMap.get(friendId).remove(userId);
        }

        log.info("Пользователи {} и {} больше не друзья",
                userId, friendId);
    }

    /**
     * Возвращает список друзей пользователя.
     * @param userId идентификатор пользователя
     * @return список друзей
     */
    public List<User> getFriends(final Long userId) {
        log.info("Получение списка друзей пользователя с id={}", userId);

        if (!userStorage.findById(userId).isPresent()) {
            throw new NotFoundException(
                    "Пользователь с id = " + userId + " не найден");
        }

        final Set<Long> friendIds = friendsMap.getOrDefault(
                userId, new HashSet<>());

        return friendIds.stream()
                .map(id -> userStorage.findById(id).orElse(null))
                .filter(user -> user != null)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает список общих друзей двух пользователей.
     * @param userId идентификатор первого пользователя
     * @param otherUserId идентификатор второго пользователя
     * @return список общих друзей
     */
    public List<User> getCommonFriends(final Long userId,
                                       final Long otherUserId) {
        log.info("Получение общих друзей: userId={}, otherUserId={}",
                userId, otherUserId);

        if (!userStorage.findById(userId).isPresent()) {
            throw new NotFoundException(
                    "Пользователь с id = " + userId + " не найден");
        }
        if (!userStorage.findById(otherUserId).isPresent()) {
            throw new NotFoundException(
                    "Пользователь с id = " + otherUserId + " не найден");
        }

        final Set<Long> userFriends = friendsMap.getOrDefault(
                userId, new HashSet<>());
        final Set<Long> otherFriends = friendsMap.getOrDefault(
                otherUserId, new HashSet<>());

        final Set<Long> commonFriendIds = new HashSet<>(userFriends);
        commonFriendIds.retainAll(otherFriends);

        return commonFriendIds.stream()
                .map(id -> userStorage.findById(id).orElse(null))
                .filter(user -> user != null)
                .collect(Collectors.toList());
    }
}
