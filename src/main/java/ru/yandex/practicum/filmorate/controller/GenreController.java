package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

/**
 * Контроллер для управления жанрами.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/genres")
public class GenreController {

    /** Хранилище фильмов (для доступа к жанрам). */
    private final FilmStorage filmStorage;

    /**
     * Возвращает все жанры.
     * @return список жанров
     */
    @GetMapping
    public List<Genre> findAllGenres() {
        log.info("Получение списка всех жанров");
        return filmStorage.findAllGenres();
    }

    /**
     * Возвращает жанр по идентификатору.
     * @param id идентификатор жанра
     * @return жанр
     */
    @GetMapping("/{id}")
    public Genre findGenreById(@PathVariable final Long id) {
        log.info("Получение жанра с id={}", id);
        return filmStorage.findGenreById(id)
                .orElseThrow(() -> {
                    log.warn("Жанр с id={} не найден", id);
                    return new NotFoundException(
                            "Жанр с id = " + id + " не найден");
                });
    }
}
