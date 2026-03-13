package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryMpaStorage implements MpaStorage {

    private final Map<Long, Mpa> mpaRatings = new ConcurrentHashMap<>();

    public InMemoryMpaStorage() {
        // Инициализируем хранилище стандартными рейтингами при запуске
        initStandardRatings();
    }

    private void initStandardRatings() {
        List<Mpa> standardRatings = Arrays.asList(
                new Mpa(1L, "G"),
                new Mpa(2L, "PG"),
                new Mpa(3L, "PG-13"),
                new Mpa(4L, "R"),
                new Mpa(5L, "NC-17")
        );

        for (Mpa mpa : standardRatings) {
            mpaRatings.put(mpa.getId(), mpa);
        }
    }

    @Override
    public List<Mpa> findAllMpa() {
        return new ArrayList<>(mpaRatings.values());
    }

    @Override
    public Optional<Mpa> findMpaById(Long id) {
        return Optional.ofNullable(mpaRatings.get(id));
    }
}

