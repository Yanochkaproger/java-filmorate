package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.ReleaseDate;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List; // Импортируем List

@Data // Эта аннотация создаст getGenres() и setGenres(List<Genre>)
public class Film {
    private Long id;

    @NotBlank(message = "Введите название фильма.")
    private String name;

    @NotNull
    @Size(max = 200, message = "Слишком длинное описание.")
    private String description;

    @NotNull
    @ReleaseDate(value = "1895-12-28", message = "Введите дату релиза не ранее 28 декабря 1895 года.")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть больше 0.")
    private Integer duration;

    @NotNull
    private Mpa mpa;

    // ВАЖНО:
    // 1. Нет слова 'final'
    // 2. Тип List<Genre>, а не LinkedHashSet
    // 3. Нет инициализации "= new ..." прямо здесь (это мешает сеттеру)
    private List<Genre> genres;
}
