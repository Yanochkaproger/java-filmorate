package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService; // Импортируем новый сервис

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/mpa")
public class MpaController {
    private final MpaService mpaService; // Заменяем FilmService на MpaService

    @GetMapping
    public List<Mpa> findAllMpa() {
        log.info("GET /mpa");
        return mpaService.findAll();
    }

    @GetMapping("/{id}")
    public Mpa findMpaById(@PathVariable("id") Long id) {
        log.info("GET /mpa/{}", id);
        // Сервис сам выбросит NotFoundException, если ID не найден
        return mpaService.findById(id);
    }
}
