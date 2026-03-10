package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    /** Хранилище дружеских связей: userId → friendId → Friendship. */
    private final Map<Long, Map<Long, Friendship>> friendships = new HashMap<>();

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
        friendships.put(created.getId(), new HashMap<>());
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
     * Отправляет запрос на добавление в друзья.
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     */
    public void addFriend(final Long userId, final Long friendId) {
        log.info("Отправка запроса в друзья: userId={}, friendId={}",
                userId, friendId);

        validateUsersExist(userId, friendId);

        // Проверяем, есть ли уже заявка от friendId к userId
        final Friendship existingFriendship = friendships
                .getOrDefault(friendId, new HashMap<>())
                .get(userId);

        if (existingFriendship != null
                && existingFriendship.getStatus() == FriendshipStatus.PENDING) {
            // Подтверждаем дружбу (взаимная заявка)
            confirmFriendship(userId, friendId);
            return;
        }

        // Создаём или обновляем заявку
        friendships.computeIfAbsent(userId, k -> new HashMap<>())
                .put(friendId, new Friendship(userId, friendId,
                        FriendshipStatus.PENDING));

        log.info("Заявка в друзья от {} к {} создана (статус: PENDING)",
                userId, friendId);
    }

    /**
     * Подтверждает дружбу между двумя пользователями.
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     */
    private void confirmFriendship(final Long userId, final Long friendId) {
        // Создаём подтверждённую связь в обе стороны
        friendships.computeIfAbsent(userId, k -> new HashMap<>())
                .put(friendId, new Friendship(userId, friendId,
                        FriendshipStatus.CONFIRMED));
        friendships.computeIfAbsent(friendId, k -> new HashMap<>())
                .put(userId, new Friendship(friendId, userId,
                        FriendshipStatus.CONFIRMED));

        log.info("Дружба между {} и {} подтверждена (статус: CONFIRMED)",
                userId, friendId);
    }

    /**
     * Подтверждает заявку на добавление в друзья.
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     */
    public void confirmFriend(final Long userId, final Long friendId) {
        log.info("Подтверждение дружбы: userId={}, friendId={}",
                userId, friendId);

        validateUsersExist(userId, friendId);

        final Friendship friendship = friendships
                .getOrDefault(friendId, new HashMap<>())
                .get(userId);

        if (friendship == null
                || friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new NotFoundException(
                    "Заявка на дружбу от пользователя с id = "
                            + friendId + " не найдена");
        }

        confirmFriendship(userId, friendId);
    }

    /**
     * Удаляет пользователя из друзей.
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     */
    public void removeFriend(final Long userId, final Long friendId) {
        log.info("Удаление из друзей: userId={}, friendId={}",
                userId, friendId);

        validateUsersExist(userId, friendId);

        if (friendships.containsKey(userId)) {
            friendships.get(userId).remove(friendId);
        }
        if (friendships.containsKey(friendId)) {
            friendships.get(friendId).remove(userId);
        }

        log.info("Дружба между {} и {} удалена", userId, friendId);
    }

    /**
     * Возвращает список друзей пользователя.
     * @param userId идентификатор пользователя
     * @return список друзей
     */
    public List<User> getFriends(final Long userId) {
        log.info("Получение списка друзей пользователя с id={}", userId);

        validateUsersExist(userId);

        final Map<Long, Friendship> userFriendships = friendships.get(userId);
        if (userFriendships == null) {
            return new ArrayList<>();
        }

        final Set<Long> friendIds = userFriendships.entrySet().stream()
                .filter(entry -> entry.getValue().getStatus()
                        == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

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

        validateUsersExist(userId, otherUserId);

        final Set<Long> userFriends = getFriendIds(userId);
        final Set<Long> otherFriends = getFriendIds(otherUserId);

        final Set<Long> commonFriendIds = new HashSet<>(userFriends);
        commonFriendIds.retainAll(otherFriends);

        return commonFriendIds.stream()
                .map(id -> userStorage.findById(id).orElse(null))
                .filter(user -> user != null)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает список входящих заявок на дружбу.
     * @param userId идентификатор пользователя
     * @return список пользователей с pending заявками
     */
    public List<User> getPendingFriends(final Long userId) {
        log.info("Получение входящих заявок пользователя с id={}", userId);

        validateUsersExist(userId);

        final Map<Long, Friendship> userFriendships = friendships.get(userId);
        if (userFriendships == null) {
            return new ArrayList<>();
        }

        final Set<Long> pendingIds = userFriendships.entrySet().stream()
                .filter(entry -> entry.getValue().getStatus()
                        == FriendshipStatus.PENDING
                        && !entry.getValue().getUserId().equals(userId))
                .map(entry -> entry.getValue().getUserId())
                .collect(Collectors.toSet());

        return pendingIds.stream()
                .map(id -> userStorage.findById(id).orElse(null))
                .filter(user -> user != null)
                .collect(Collectors.toList());
    }

    /**
     * Получает идентификаторы подтверждённых друзей пользователя.
     * @param userId идентификатор пользователя
     * @return множество идентификаторов друзей
     */
    private Set<Long> getFriendIds(final Long userId) {
        final Map<Long, Friendship> userFriendships = friendships.get(userId);
        if (userFriendships == null) {
            return new HashSet<>();
        }

        return userFriendships.entrySet().stream()
                .filter(entry -> entry.getValue().getStatus()
                        == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Проверяет существование пользователей.
     * @param userIds идентификаторы пользователей
     */
    private void validateUsersExist(final Long... userIds) {
        for (final Long userId : userIds) {
            if (!userStorage.findById(userId).isPresent()) {
                log.warn("Пользователь с id={} не найден", userId);
                throw new NotFoundException(
                        "Пользователь с id = " + userId + " не найден");
            }
        }
    }
}

