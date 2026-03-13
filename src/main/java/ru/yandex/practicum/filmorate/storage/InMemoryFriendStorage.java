package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryFriendStorage implements FriendStorage {

    // Хранилище дружбы: Key = userId, Value = Set of friendIds (кого добавил сам пользователь)
    private final Map<Long, Set<Long>> friendsMap = new ConcurrentHashMap<>();

    private final UserStorage userStorage;

    public InMemoryFriendStorage(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        // ОДНОСТОРОННЕЕ ДЕЙСТВИЕ:
        // Добавляем friendId в список друзей userId.
        // friendId при этом userId к себе НЕ добавляет (ждем подтверждения).
        friendsMap.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(friendId);

        // ВТОРАЯ СТРОКА (ВЗАИМНОСТЬ) УДАЛЕНА!
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        // ОДНОСТОРОННЕЕ ДЕЙСТВИЕ:
        // Удаляем friendId из списка друзей userId.
        Set<Long> userFriends = friendsMap.get(userId);
        if (userFriends != null) {
            userFriends.remove(friendId);
        }

        // ВТОРАЯ СТРОКА (УДАЛЕНИЕ У ДРУГА) УДАЛЕНА!
        // Если friendId ранее добавил userId, то userId так и останется в списке у friendId.
    }

    @Override
    public List<User> findAllFriends(Long id) {
        // Возвращаем список тех, кого пользователь id добавил себе в друзья
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
        // Получаем списки друзей обоих пользователей
        Set<Long> userFriends = friendsMap.getOrDefault(id, Collections.emptySet());
        Set<Long> otherFriends = friendsMap.getOrDefault(otherId, Collections.emptySet());

        // Находим пересечение: тех, кого добавили ОБА пользователя друг другу (или третьих лиц)
        // В контексте "общих друзей" обычно имеются в виду третьи лица, которые есть в друзьях и у того, и у другого.
        Set<Long> commonIds = new HashSet<>(userFriends);
        commonIds.retainAll(otherFriends);

        return commonIds.stream()
                .map(fid -> userStorage.findUserById(fid).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
