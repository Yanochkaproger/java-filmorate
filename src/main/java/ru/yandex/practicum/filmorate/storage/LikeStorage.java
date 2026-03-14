package ru.yandex.practicum.filmorate.storage;

public interface LikeStorage {
    /**
     * Добавить лайк фильму от пользователя.
     * @param id ID фильма
     * @param userId ID пользователя
     */
    void addLike(Long id, Long userId);

    /**
     * Удалить лайк у фильма от пользователя.
     * @param id ID фильма
     * @param userId ID пользователя
     */
    void removeLike(Long id, Long userId);

    /**
     * Получить количество лайков у фильма.
     * Необходим для сортировки популярных фильмов.
     * @param filmId ID фильма
     * @return количество лайков
     */
    int getLikeCount(Long filmId);
}
