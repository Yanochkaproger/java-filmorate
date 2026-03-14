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

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final LikeStorage likeStorage;
    private final UserStorage userStorage;

    public Film create(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            Mpa mpa = mpaStorage.findMpaById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id=" + film.getMpa().getId() + " не найден"));
            film.setMpa(mpa);
        } else {
            film.setMpa(null);
        }

        List<Genre> validGenres = new ArrayList<>();
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> uniqueIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (!uniqueIds.isEmpty()) {
                Map<Long, Genre> genreMap = genreStorage.findAllGenres().stream()
                        .collect(Collectors.toMap(Genre::getId, g -> g));

                for (Long id : uniqueIds) {
                    Genre g = genreMap.get(id);
                    if (g == null) {
                        throw new NotFoundException("Жанр с id=" + id + " не найден");
                    }
                    validGenres.add(g);
                }
            }
        }
        film.setGenres(validGenres);

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        filmStorage.findFilmById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + film.getId() + " не найден"));

        if (film.getMpa() != null && film.getMpa().getId() != null) {
            Mpa mpa = mpaStorage.findMpaById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id=" + film.getMpa().getId() + " не найден"));
            film.setMpa(mpa);
        } else {
            film.setMpa(null);
        }

        List<Genre> validGenres = new ArrayList<>();
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> uniqueIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (!uniqueIds.isEmpty()) {
                Map<Long, Genre> genreMap = genreStorage.findAllGenres().stream()
                        .collect(Collectors.toMap(Genre::getId, g -> g));

                for (Long id : uniqueIds) {
                    Genre g = genreMap.get(id);
                    if (g == null) {
                        throw new NotFoundException("Жанр с id=" + id + " не найден");
                    }
                    validGenres.add(g);
                }
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

    public List<Film> findPopular(int count) {
        // Сортировка теперь выполняется на уровне БД одним запросом (SQL ORDER BY)
        // Это избавляет от вызовов getLikeCount в цикле при сортировке
        List<Film> films = filmStorage.findPopular(count);
        films.forEach(this::enrichFilmData);
        return films;
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

        if (film.getMpa() != null && film.getMpa().getId() != null && film.getMpa().getName() == null) {
            mpaStorage.findMpaById(film.getMpa().getId()).ifPresent(film::setMpa);
        }

        List<Genre> dbGenres = genreStorage.findGenresByFilmId(film.getId());
        film.setGenres(dbGenres);
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