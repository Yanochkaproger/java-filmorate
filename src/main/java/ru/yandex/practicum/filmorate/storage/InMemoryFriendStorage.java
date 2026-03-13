package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryFriendStorage implements FriendStorage {

    // Хранилище дружбы: Key = userId, Value = Set of friendIds
    private final Map<Long, Set<Long>> friendsMap = new ConcurrentHashMap<>();

    // Ссылка на хранилище пользователей
    private final UserStorage userStorage;

    public InMemoryFriendStorage(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        // Добавляем друга пользователю
        friendsMap.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(friendId);
        // Добавляем пользователя другу (ВЗАИМНОСТЬ)
        friendsMap.computeIfAbsent(friendId, k -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        // Удаляем друга у пользователя
        Set<Long> userFriends = friendsMap.get(userId);
        if (userFriends != null) {
            userFriends.remove(friendId);
        }

        // Удаляем пользователя у друга (ВЗАИМНОСТЬ)
        Set<Long> friendFriends = friendsMap.get(friendId);
        if (friendFriends != null) {
            friendFriends.remove(userId);
        }
    }

    @Override
    public List<User> findAllFriends(Long id) {
        Set<Long> friendIds = friendsMap.get(id);
        if (friendIds == null || friendIds.isEmpty()) {
            return new ArrayList<>();
        }

        return friendIds.stream()
                .map(fid -> userStorage.findUserById(fid).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findCommonFriends(Long id, Long otherId) {
        Set<Long> userFriends = friendsMap.getOrDefault(id, Collections.emptySet());
        Set<Long> otherFriends = friendsMap.getOrDefault(otherId, Collections.emptySet());

        // Находим пересечение множеств
        Set<Long> commonIds = new HashSet<>(userFriends);
        commonIds.retainAll(otherFriends);

        return commonIds.stream()
                .map(fid -> userStorage.findUserById(fid).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
