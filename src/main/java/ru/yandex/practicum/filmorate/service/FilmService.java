package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final LikeStorage likeStorage;
    private final UserStorage userStorage;

    public Film create(Film film) {
        // 1. Обработка и валидация MPA
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            Mpa mpa = mpaStorage.findMpaById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id=" + film.getMpa().getId() + " не найден"));
            film.setMpa(mpa);
        } else {
            film.setMpa(null);
        }

        // 2. Обработка и валидация Жанров (с удалением дубликатов)
        List<Genre> validGenres = new ArrayList<>();
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> uniqueIds = new LinkedHashSet<>();
            for (Genre g : film.getGenres()) {
                if (g.getId() != null) {
                    uniqueIds.add(g.getId());
                }
            }
            for (Long id : uniqueIds) {
                Genre g = genreStorage.findGenreById(id)
                        .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
                validGenres.add(g);
            }
        }
        film.setGenres(validGenres);

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        // Проверка существования фильма
        filmStorage.findFilmById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + film.getId() + " не найден"));

        // Обработка MPA
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            Mpa mpa = mpaStorage.findMpaById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id=" + film.getMpa().getId() + " не найден"));
            film.setMpa(mpa);
        } else {
            film.setMpa(null);
        }

        // Обработка Жанров
        List<Genre> validGenres = new ArrayList<>();
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> uniqueIds = new LinkedHashSet<>();
            for (Genre g : film.getGenres()) {
                if (g.getId() != null) {
                    uniqueIds.add(g.getId());
                }
            }
            for (Long id : uniqueIds) {
                Genre g = genreStorage.findGenreById(id)
                        .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
                validGenres.add(g);
            }
        }
        film.setGenres(validGenres);

        return filmStorage.update(film);
    }

    public Film findFilmById(Long id) {
        Film film = filmStorage.findFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
        enrichFilmData(film);
        return film;
    }

    public List<Film> findAllFilms() {
        List<Film> films = filmStorage.findAllFilms();
        films.forEach(this::enrichFilmData);
        return films;
    }

    // !!! ИСПРАВЛЕНИЕ ЗДЕСЬ !!!
    // Сортировка теперь происходит в сервисе, используя likeStorage
    public List<Film> findPopular(int count) {
        // 1. Берем ВСЕ фильмы из хранилища
        List<Film> films = filmStorage.findAllFilms();

        // 2. Сортируем их по количеству лайков (используя метод getLikeCount из LikeStorage)
        return films.stream()
                .sorted((f1, f2) -> {
                    int likes1 = likeStorage.getLikeCount(f1.getId());
                    int likes2 = likeStorage.getLikeCount(f2.getId());
                    return Integer.compare(likes2, likes1); // По убыванию
                })
                .limit(count)
                .peek(this::enrichFilmData) // Обязательно обогащаем данными (MPA, Genres)!
                .collect(Collectors.toList());
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        likeStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        likeStorage.removeLike(filmId, userId);
    }

    private void enrichFilmData(Film film) {
        if (film == null) return;

        // Подгрузка MPA
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaStorage.findMpaById(film.getMpa().getId()).ifPresent(film::setMpa);
        }

        // Подгрузка Жанров
        List<Genre> currentGenres = film.getGenres();
        if (currentGenres != null && !currentGenres.isEmpty()) {
            List<Genre> fullGenres = new ArrayList<>();
            for (Genre g : currentGenres) {
                if (g.getId() != null) {
                    if (g.getName() == null) {
                        genreStorage.findGenreById(g.getId()).ifPresent(fullGenres::add);
                    } else {
                        fullGenres.add(g);
                    }
                }
            }
            if (!fullGenres.isEmpty()) {
                film.setGenres(fullGenres);
            }
        } else {
            if (film.getGenres() == null) {
                film.setGenres(new ArrayList<>());
            }
        }
    }

    public List<Mpa> findAllMpa() {
        return mpaStorage.findAllMpa();
    }

    public Mpa findMpaById(Long id) {
        return mpaStorage.findMpaById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id=" + id + " не найден"));
    }

    public List<Genre> findAllGenres() {
        return genreStorage.findAllGenres();
    }

    public Genre findGenreById(Long id) {
        return genreStorage.findGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
    }
}
