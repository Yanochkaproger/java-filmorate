package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryLikeStorage implements LikeStorage {

    // Храним лайки: Key = filmId, Value = Set of userIds
    // Используем Set, чтобы один пользователь не мог лайкнуть фильм дважды
    private final Map<Long, Set<Long>> likesMap = new ConcurrentHashMap<>();

    @Override
    public void addLike(Long id, Long userId) {
        // computeIfAbsent создаст новый Set, если для фильма еще нет лайков
        likesMap.computeIfAbsent(id, k -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    @Override
    public void removeLike(Long id, Long userId) {
        Set<Long> filmLikes = likesMap.get(id);
        if (filmLikes != null) {
            filmLikes.remove(userId);
            // Опционально: удаляем запись о фильме, если лайков больше нет, чтобы не засорять память
            if (filmLikes.isEmpty()) {
                likesMap.remove(id);
            }
        }
    }

    @Override
    public int getLikeCount(Long filmId) {
        Set<Long> filmLikes = likesMap.get(filmId);
        return filmLikes == null ? 0 : filmLikes.size();
    }

    // Вспомогательный метод (можно оставить, если используется где-то еще)
    public Set<Long> getLikes(Long filmId) {
        return likesMap.getOrDefault(filmId, ConcurrentHashMap.newKeySet());
    }
}

