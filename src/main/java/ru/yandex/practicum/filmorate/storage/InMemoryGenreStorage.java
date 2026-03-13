package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGenreStorage implements GenreStorage {

    private final Map<Long, Genre> genres = new ConcurrentHashMap<>();

    public InMemoryGenreStorage() {
        // Инициализируем хранилище стандартными жанрами при запуске
        initStandardGenres();
    }

    private void initStandardGenres() {
        List<Genre> standardGenres = Arrays.asList(
                new Genre(1L, "Комедия"),
                new Genre(2L, "Драма"),
                new Genre(3L, "Мультфильм"),
                new Genre(4L, "Триллер"),
                new Genre(5L, "Документальный"),
                new Genre(6L, "Боевик")
        );

        for (Genre genre : standardGenres) {
            genres.put(genre.getId(), genre);
        }
    }

    @Override
    public List<Genre> findAllGenres() {
        return new ArrayList<>(genres.values());
    }

    @Override
    public Optional<Genre> findGenreById(Long id) {
        return Optional.ofNullable(genres.get(id));
    }

    @Override
    public void findAllGenresByFilm(List<Film> films) {
        // В текущей реализации in-memory этот метод может быть пустым или служить заглушкой,
        // так как обычно он используется для обогащения списка фильмов жанрами из БД.
        // Если логика требует простого пропуска — оставляем пустым.
        // Если нужно что-то специфическое для тестов — можно добавить логику позже.
        // Пока реализуем как no-op (ничего не делаем), чтобы удовлетворить интерфейс.
    }
}
