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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.Set;

/**
 * Контроллер для управления фильмами.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

    /** Сервис для работы с фильмами. */
    private final FilmService filmService;

    /**
     * Возвращает все фильмы.
     * @return список фильмов
     */
    @GetMapping
    public List<Film> findAll() {
        return filmService.findAll();
    }

    /**
     * Возвращает фильм по идентификатору.
     * @param id идентификатор фильма
     * @return фильм
     */
    @GetMapping("/{id}")
    public Film findById(@PathVariable final Long id) {
        return filmService.findById(id);
    }

    /**
     * Создаёт новый фильм.
     * @param film данные фильма из тела запроса
     * @return созданный фильм
     */
    @PostMapping
    public Film create(final @RequestBody Film film) {
        return filmService.create(film);
    }

    /**
     * Обновляет существующий фильм.
     * @param film данные для обновления
     * @return обновлённый фильм
     */
    @SuppressWarnings("unused")
    @PutMapping
    public Film update(final @RequestBody Film film) {
        return filmService.update(film);
    }

    /**
     * Добавляет лайк фильму.
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     */
    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(@PathVariable final Long filmId,
                        @PathVariable final Long userId) {
        filmService.addLike(filmId, userId);
    }

    /**
     * Удаляет лайк у фильма.
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     */
    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLike(@PathVariable final Long filmId,
                           @PathVariable final Long userId) {
        filmService.removeLike(filmId, userId);
    }

    /**
     * Возвращает список идентификаторов пользователей,
     * лайкнувших фильм.
     * @param filmId идентификатор фильма
     * @return список userId
     */
    @GetMapping("/{filmId}/likes")
    public Set<Long> getLikes(@PathVariable final Long filmId) {
        return filmService.getLikes(filmId);
    }

    /**
     * Возвращает 10 наиболее популярных фильмов.
     * @param count количество фильмов (необязательный)
     * @return список популярных фильмов
     */
    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(required = false) final Integer count) {
        return filmService.getPopularFilms(count);
    }
}
