package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserRepositoryTest {
    private final UserRepository userRepository;
    private final User user = User.builder()
        .id(1L)
        .name("user1")
        .email("mail@yandex.ru")
        .build();

    @BeforeEach
    public void beforeEach() {
        userRepository.save(user);
    }

    @Nested
    @DisplayName("Проверка дубликата почты пользователя")
    class ExistsByEmail {
        @Test
        @DisplayName("Вернет: true, user с такой почтой существует")
        public void shouldGetTrueTest() {
            boolean result = userRepository.existsByEmail(user.getEmail());
            assertTrue(result);
        }

        @Test
        @DisplayName("Вернет: false, user с такой почтой не существует")
        public void shouldGetFalseTest() {
            boolean result = userRepository.existsByEmail("email@email.ru");
            assertFalse(result);
        }
    }
}