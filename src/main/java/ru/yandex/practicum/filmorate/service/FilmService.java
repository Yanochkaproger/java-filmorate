package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Сервис для управления фильмами и лайками.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    /** Хранилище фильмов. */
    private final FilmStorage filmStorage;

    /** Хранилище пользователей (для проверки существования). */
    private final UserStorage userStorage;

    /** Хранилище лайков: filmId → набор userId. */
    private final Map<Long, Set<Long>> likesMap = new HashMap<>();

    /**
     * Возвращает все фильмы.
     * @return список фильмов
     */
    public List<Film> findAll() {
        log.info("Получение списка всех фильмов");
        return new ArrayList<>(filmStorage.findAll());
    }

    /**
     * Возвращает фильм по идентификатору.
     * @param id идентификатор фильма
     * @return фильм
     */
    public Film findById(final Long id) {
        log.info("Получение фильма с id={}", id);
        return filmStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id={} не найден", id);
                    return new NotFoundException(
                            "Фильм с id = " + id + " не найден");
                });
    }

    /**
     * Создаёт новый фильм.
     * @param film данные фильма
     * @return созданный фильм
     */
    public Film create(final Film film) {
        log.info("Создание фильма: {}", film.getName());
        final Film created = filmStorage.create(film);
        likesMap.put(created.getId(), new HashSet<>());
        log.info("Фильм с id={} успешно создан", created.getId());
        return created;
    }

    /**
     * Обновляет существующий фильм.
     * @param film данные для обновления
     * @return обновлённый фильм
     */
    public Film update(final Film film) {
        log.info("Обновление фильма с id={}", film.getId());
        final Film updated = filmStorage.update(film);
        log.info("Фильм с id={} успешно обновлён", updated.getId());
        return updated;
    }

    /**
     * Добавляет лайк фильму.
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     */
    public void addLike(final Long filmId, final Long userId) {
        log.info("Добавление лайка: filmId={}, userId={}",
                filmId, userId);

        // ✅ Проверяем существование фильма
        if (!filmStorage.findById(filmId).isPresent()) {
            log.warn("Фильм с id={} не найден", filmId);
            throw new NotFoundException(
                    "Фильм с id = " + filmId + " не найден");
        }

        // ✅ Проверяем существование пользователя
        if (!userStorage.findById(userId).isPresent()) {
            log.warn("Пользователь с id={} не найден", userId);
            throw new NotFoundException(
                    "Пользователь с id = " + userId + " не найден");
        }

        likesMap.computeIfAbsent(filmId, k -> new HashSet<>())
                .add(userId);

        log.info("Пользователь {} лайкнул фильм {}", userId, filmId);
    }

    /**
     * Удаляет лайк у фильма.
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     */
    public void removeLike(final Long filmId, final Long userId) {
        log.info("Удаление лайка: filmId={}, userId={}",
                filmId, userId);

        // ✅ Проверяем существование фильма
        if (!filmStorage.findById(filmId).isPresent()) {
            log.warn("Фильм с id={} не найден", filmId);
            throw new NotFoundException(
                    "Фильм с id = " + filmId + " не найден");
        }

        // ✅ Проверяем существование пользователя
        if (!userStorage.findById(userId).isPresent()) {
            log.warn("Пользователь с id={} не найден", userId);
            throw new NotFoundException(
                    "Пользователь с id = " + userId + " не найден");
        }

        if (likesMap.containsKey(filmId)) {
            likesMap.get(filmId).remove(userId);
        }

        log.info("Пользователь {} удалил лайк у фильма {}",
                userId, filmId);
    }

    /**
     * Возвращает список идентификаторов пользователей,
     * лайкнувших фильм.
     * @param filmId идентификатор фильма
     * @return список userId
     */
    public Set<Long> getLikes(final Long filmId) {
        log.info("Получение лайков фильма с id={}", filmId);

        if (!filmStorage.findById(filmId).isPresent()) {
            log.warn("Фильм с id={} не найден", filmId);
            throw new NotFoundException(
                    "Фильм с id = " + filmId + " не найден");
        }

        return new HashSet<>(likesMap.getOrDefault(
                filmId, new HashSet<>()));
    }

    /**
     * Возвращает 10 наиболее популярных фильмов
     * по количеству лайков.
     * @param count количество фильмов (по умолчанию 10)
     * @return список популярных фильмов
     */
    public List<Film> getPopularFilms(final Integer count) {
        final int limit = count != null ? count : 10;

        log.info("Получение {} популярных фильмов", limit);

        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> {
                    final int likes1 = likesMap.getOrDefault(
                            f1.getId(), new HashSet<>()).size();
                    final int likes2 = likesMap.getOrDefault(
                            f2.getId(), new HashSet<>()).size();
                    return Integer.compare(likes2, likes1);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }
}
