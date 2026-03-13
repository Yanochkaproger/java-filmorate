package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository // <-- ЭТА АННОТАЦИЯ КРИТИЧЕСКИ ВАЖНА! Она регистрирует класс как би́н в Spring
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final AtomicLong filmIdCounter = new AtomicLong(1);

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
            throw new RuntimeException("Фильм с таким ID не найден для обновления");
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
        // Пока можно вернуть просто первые N фильмов, логику популярности добавите позже
        return films.values().stream()
                .limit(count)
                .toList();
    }

    @Override
    public Optional<Film> findFilmById(Long id) {
        return Optional.ofNullable(films.get(id));
    }
}
