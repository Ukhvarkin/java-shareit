package ru.practicum.shareit.item.comment;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CommentRepositoryTest {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LocalDateTime dateTime = LocalDateTime.of(2000, 1, 1, 1, 1, 1);

    private final User user1 = User.builder()
        .id(1L)
        .name("user1")
        .email("one@yandex.ru")
        .build();
    private final User user2 = User.builder()
        .id(2L)
        .name("user2")
        .email("two@yandex.ru")
        .build();
    private final User user3 = User.builder()
        .id(3L)
        .name("user3")
        .email("three@yandex.ru")
        .build();
    private final Item item1 = Item.builder()
        .id(1L)
        .name("item1")
        .description("description")
        .available(true)
        .owner(user1)
        .build();
    private final Comment comment1 = Comment.builder()
        .id(1L)
        .text("text")
        .created(dateTime.plusYears(5))
        .author(user3)
        .item(item1)
        .build();

    private final Comment comment2 = Comment.builder()
        .id(2L)
        .text("text")
        .created(dateTime.plusYears(6))
        .author(user2)
        .item(item1)
        .build();

    @BeforeEach
    public void beforeEach() {
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        itemRepository.save(item1);
        commentRepository.save(comment1);
        commentRepository.save(comment2);
    }

    @Nested
    @DisplayName("Поиск вещи по названию или описанию")
    class GtAllByItemIdOrderByCreatedAsc {
        @Test
        @DisplayName("Положительный поиск по буквам: IT")
        public void searchTest() {
            List<Comment> comments = commentRepository.getAllByItemIdOrderByCreatedAsc(item1.getId());

            assertEquals(2, comments.size());

            Comment commentFromRepository1 = comments.get(0);
            Comment commentFromRepository2 = comments.get(1);

            assertEquals(comment1, commentFromRepository1);
            assertEquals(comment2, commentFromRepository2);
        }
    }
}