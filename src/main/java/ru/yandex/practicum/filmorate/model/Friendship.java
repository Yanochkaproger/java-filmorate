package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель дружеской связи между двумя пользователями.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {

    /** Идентификатор пользователя, который отправил запрос. */
    private Long userId;

    /** Идентификатор пользователя, которому отправлен запрос. */
    private Long friendId;

    /** Статус дружбы. */
    private FriendshipStatus status;
}
