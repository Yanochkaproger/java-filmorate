package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

/**
 * Интерфейс хранилища пользователей.
 */
public interface UserStorage {

    /**
     * Возвращает всех пользователей.
     * @return коллекция пользователей
     */
    Collection<User> findAll();

    /**
     * Возвращает пользователя по идентификатору.
     * @param id идентификатор пользователя
     * @return Optional с пользователем
     */
    Optional<User> findById(Long id);

    /**
     * Добавляет нового пользователя.
     * @param user пользователь для добавления
     * @return сохранённый пользователь с установленным id
     */
    User create(User user);

    /**
     * Обновляет существующего пользователя.
     * @param user пользователь для обновления
     * @return обновлённый пользователь
     */
    User update(User user);

    /**
     * Удаляет пользователя по идентификатору.
     * @param id идентификатор пользователя
     */
    void delete(Long id);
}
