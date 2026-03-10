package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Тесты для проверки запуска приложения.
 */
@SpringBootTest(classes = FilmorateApplication.class)
class FilmorateApplicationTests {

	/**
	 * Проверка, что контекст приложения загружается.
	 */
	@Test
	void contextLoads() {
		// Тест проходит, если контекст загрузился успешно
	}
}