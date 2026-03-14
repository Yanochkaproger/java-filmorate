package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users") // Добавлен слэш в начале для корректности пути
public class UserController {
    private final UserService userService;

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        // Исправление: сохраняем результат работы сервиса (там может быть присвоен ID)
        User createdUser = userService.create(user);
        log.info("POST /users/{}", createdUser.getId());
        return createdUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        // Исправление: возвращаем обновленный объект из сервиса
        User updatedUser = userService.update(user);
        log.info("PUT /users/{}", updatedUser.getId());
        return updatedUser;
    }

    @GetMapping
    public List<User> findAll() {
        log.info("GET /users");
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findUserById(@PathVariable("id") Long id) {
        log.info("GET /users/{}", id);
        return userService.findUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable("id") Long id, @PathVariable("friendId") Long friendId) {
        log.info("PUT /users/{}/friends/{}", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable("id") Long id, @PathVariable("friendId") Long friendId) {
        log.info("DELETE /users/{}/friends/{}", id, friendId); // Было PUT в логе
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable("id") Long id) {
        log.info("GET /users/{}/friends", id);
        // Исправление: используем параметр id, а не несуществующий userId
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable("id") Long id, @PathVariable("otherId") Long otherId) {
        log.info("GET /users/{}/friends/common/{}", id, otherId);
        // Исправление: используем параметры id и otherId
        return userService.getCommonFriends(id, otherId);
    }
}


