package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

/**
 * Контроллер для управления пользователями.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    /** Сервис для работы с пользователями. */
    private final UserService userService;

    /**
     * Возвращает всех пользователей.
     * @return список пользователей
     */
    @GetMapping
    public List<User> findAll() {
        return userService.findAll();
    }

    /**
     * Возвращает пользователя по идентификатору.
     * @param id идентификатор пользователя
     * @return пользователь
     */
    @GetMapping("/{id}")
    public User findById(@PathVariable final Long id) {
        return userService.findById(id);
    }

    /**
     * Создаёт нового пользователя.
     * @param user данные пользователя из тела запроса
     * @return созданный пользователь
     */
    @PostMapping
    public User create(final @RequestBody User user) {
        return userService.create(user);
    }

    /**
     * Обновляет существующего пользователя.
     * @param user данные для обновления
     * @return обновлённый пользователь
     */
    @SuppressWarnings("unused")
    @PutMapping
    public User update(final @RequestBody User user) {
        log.info("Попытка обновления пользователя с id={}", user.getId());
        validateUser(user);
        if (user.getId() == null || !users.containsKey(user.getId())) {
            log.error("Пользователь с id={} не найден", user.getId());
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.info("Пользователь с id={} успешно обновлён", user.getId());
        return user;
        return userService.update(user);
    }

    /**
     * Добавляет пользователя в друзья.
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     */
    @PutMapping("/{userId}/friends/{friendId}")
    public void addFriend(@PathVariable final Long userId,
                          @PathVariable final Long friendId) {
        userService.addFriend(userId, friendId);
    }

    /**
     * Удаляет пользователя из друзей.
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     */
    @DeleteMapping("/{userId}/friends/{friendId}")
    public void removeFriend(@PathVariable final Long userId,
                             @PathVariable final Long friendId) {
        userService.removeFriend(userId, friendId);
    }

    /**
     * Возвращает список друзей пользователя.
     * @param userId идентификатор пользователя
     * @return список друзей
     */
    @GetMapping("/{userId}/friends")
    public List<User> getFriends(@PathVariable final Long userId) {
        return userService.getFriends(userId);
    }

    /**
     * Возвращает список общих друзей двух пользователей.
     * @param userId идентификатор первого пользователя
     * @param otherUserId идентификатор второго пользователя
     * @return список общих друзей
     */
    @GetMapping("/{userId}/friends/common/{otherUserId}")
    public List<User> getCommonFriends(@PathVariable final Long userId,
                                       @PathVariable final Long otherUserId) {
        return userService.getCommonFriends(userId, otherUserId);
    }
}
