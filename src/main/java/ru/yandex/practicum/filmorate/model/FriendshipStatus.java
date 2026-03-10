package ru.yandex.practicum.filmorate.model;

/**
 * Статус дружбы между пользователями.
 */
public enum FriendshipStatus {

    /** Заявка на добавление в друзья отправлена, но не подтверждена. */
    PENDING,

    /** Дружба подтверждена обоими пользователями. */
    CONFIRMED
}
