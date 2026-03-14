package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService; // Импортируем новый сервис

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/genres")
public class GenreController {
    private final GenreService genreService; // Заменяем FilmService на GenreService

    @GetMapping
    public List<Genre> findAllGenres() {
        log.info("GET /genres");
        return genreService.findAll();
    }

    @GetMapping("/{id}")
    public Genre findGenreById(@PathVariable("id") Long id) {
        log.info("GET /genres/{}", id);
        // Сервис сам выбросит NotFoundException, если ID не найден
        return genreService.findById(id);
    }
}
