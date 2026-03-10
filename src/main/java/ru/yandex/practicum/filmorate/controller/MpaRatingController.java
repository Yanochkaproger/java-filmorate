package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

/**
 * Контроллер для управления рейтингами MPA.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class MpaRatingController {

    /** Хранилище фильмов (для доступа к рейтингам). */
    private final FilmStorage filmStorage;

    /**
     * Возвращает все рейтинги MPA.
     * @return список рейтингов
     */
    @GetMapping
    public List<MpaRating> findAllMpaRatings() {
        log.info("Получение списка всех рейтингов MPA");
        return filmStorage.findAllMpaRatings();
    }

    /**
     * Возвращает рейтинг MPA по идентификатору.
     * @param id идентификатор рейтинга
     * @return рейтинг
     */
    @GetMapping("/{id}")
    public MpaRating findMpaRatingById(@PathVariable final Long id) {
        log.info("Получение рейтинга MPA с id={}", id);
        return filmStorage.findMpaRatingById(id)
                .orElseThrow(() -> {
                    log.warn("Рейтинг MPA с id={} не найден", id);
                    return new NotFoundException(
                            "Рейтинг MPA с id = " + id + " не найден");
                });
    }
}
