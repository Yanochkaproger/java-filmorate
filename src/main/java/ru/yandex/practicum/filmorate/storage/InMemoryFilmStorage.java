package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final AtomicLong filmIdCounter = new AtomicLong(1);

    // Нам нужно хранилище лайков, чтобы посчитать популярность
    private final LikeStorage likeStorage;

    // Конструктор с внедрением LikeStorage
    public InMemoryFilmStorage(LikeStorage likeStorage) {
        this.likeStorage = likeStorage;
    }

    @Override
    public Film create(Film film) {
        Long id = filmIdCounter.getAndIncrement();
        film.setId(id);
        films.put(id, film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null || !films.containsKey(film.getId())) {
            // ВАЖНО: Выбрасываем NotFoundException для получения статуса 404
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> findAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public List<Film> findPopular(int count) {
        // Считаем количество лайков для каждого фильма
        return films.values().stream()
                .sorted((f1, f2) -> {
                    int likes1 = likeStorage.getLikeCount(f1.getId());
                    int likes2 = likeStorage.getLikeCount(f2.getId());
                    // Сортировка по убыванию лайков
                    return Integer.compare(likes2, likes1);
                })
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Film> findFilmById(Long id) {
        return Optional.ofNullable(films.get(id));
    }
}

