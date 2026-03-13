package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    public List<Mpa> findAll() {
        return mpaStorage.findAllMpa();
    }

    public Mpa findById(Long id) {
        return mpaStorage.findMpaById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id=" + id + " не найден"));
    }
}
