package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

/**
 * Интерфейс хранилища фильмов.
 */
public interface FilmStorage {

    /**
     * Возвращает все фильмы.
     * @return коллекция фильмов
     */
    Collection<Film> findAll();

    /**
     * Возвращает фильм по идентификатору.
     * @param id идентификатор фильма
     * @return Optional с фильмом
     */
    Optional<Film> findById(Long id);

    /**
     * Добавляет новый фильм.
     * @param film фильм для добавления
     * @return сохранённый фильм с установленным id
     */
    Film create(Film film);

    /**
     * Обновляет существующий фильм.
     * @param film фильм для обновления
     * @return обновлённый фильм
     */
    Film update(Film film);

    /**
     * Удаляет фильм по идентификатору.
     * @param id идентификатор фильма
     */
    void delete(Long id);
}
