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
            film.setMpa(mpa); // Подставляем полный объект с именем
        } else {
            film.setMpa(null);
        }

        // 2. Обработка и валидация Жанров (с удалением дубликатов)
        List<Genre> validGenres = new ArrayList<>();
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            // Используем LinkedHashSet для сохранения порядка и удаления дубликатов по ID
            Set<Long> uniqueIds = new LinkedHashSet<>();
            for (Genre g : film.getGenres()) {
                if (g.getId() != null) {
                    uniqueIds.add(g.getId());
                }
            }

            for (Long id : uniqueIds) {
                Genre g = genreStorage.findGenreById(id)
                        .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
                validGenres.add(g); // Подставляем полный объект с именем
            }
        }
        film.setGenres(validGenres);

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        // Проверка существования фильма
        filmStorage.findFilmById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + film.getId() + " не найден"));

        // Обработка MPA (как в create)
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            Mpa mpa = mpaStorage.findMpaById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id=" + film.getMpa().getId() + " не найден"));
            film.setMpa(mpa);
        } else {
            film.setMpa(null);
        }

        // Обработка Жанров (как в create)
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

    public List<Film> findPopular(int count) {
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

    // Вспомогательный метод для гарантированной подгрузки имен MPA и Жанров
    private void enrichFilmData(Film film) {
        if (film == null) return;

        // 1. Подгрузка MPA
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            // Всегда пытаемся загрузить полный объект, даже если имя уже есть (на случай кэша/проблем)
            mpaStorage.findMpaById(film.getMpa().getId()).ifPresent(film::setMpa);
        }

        // 2. Подгрузка Жанров
        // Мы всегда перезагружаем жанры по ID, чтобы гарантировать наличие имени
        List<Genre> currentGenres = film.getGenres();
        if (currentGenres != null && !currentGenres.isEmpty()) {
            List<Genre> fullGenres = new ArrayList<>();
            for (Genre g : currentGenres) {
                if (g.getId() != null) {
                    // Если имя null ИЛИ мы хотим перестраховаться - грузим из хранилища
                    if (g.getName() == null) {
                        genreStorage.findGenreById(g.getId()).ifPresent(fullGenres::add);
                    } else {
                        fullGenres.add(g);
                    }
                }
            }
            // Обновляем список только если мы что-то нашли (чтобы не потерять данные)
            if (!fullGenres.isEmpty()) {
                film.setGenres(fullGenres);
            }
        } else {
            // Если список жанров пуст или null, значит они хранятся отдельно (в БД).
            // В рамках InMemory или простой реализации, если хранилище не вернуло жанры,
            // мы не можем их узнать без отдельного метода findGenresByFilmId.
            // НО! Так как мы правильно сохраняем их в create/update, они должны быть в объекте.
            // Если их нет - оставляем пустой список.
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
