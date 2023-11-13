package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRepositoryTest {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final int from = 0;
    private final int size = 10;
    private final Pageable pageable = PageRequest.of(from / size, size);
    private final User user1 = User.builder()
        .id(1L)
        .name("user1")
        .email("mail@yandex.ru")
        .build();
    private final User user2 = User.builder()
        .id(2L)
        .name("user2")
        .email("email@yandex.ru")
        .build();
    private final Item item1 = Item.builder()
        .id(1L)
        .name("item1")
        .description("description")
        .available(true)
        .owner(user1)
        .build();
    private final Item item2 = Item.builder()
        .id(2L)
        .name("item2")
        .description("описание")
        .available(true)
        .owner(user2)
        .build();

    private final Item item3 = Item.builder()
        .id(3L)
        .name("item3")
        .description("описание")
        .available(false)
        .owner(user2)
        .build();

    @BeforeEach
    public void beforeEach() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
    }

    @Nested
    @DisplayName("Поиск вещи по названию или описанию")
    class Search {
        @Test
        @DisplayName("Положительный поиск по буквам: IT")
        public void searchTest() {
            List<Item> itemFromRepository = itemRepository.search("IT", pageable).toList();

            assertEquals(2, itemFromRepository.size());

            Item itemFromRepository1 = itemFromRepository.get(0);
            Item itemFromRepository2 = itemFromRepository.get(1);

            assertEquals(item1, itemFromRepository1);
            assertEquals(item2, itemFromRepository2);
        }

        @Test
        @DisplayName("Негативный поиск по слову: Отвертка")
        public void shouldGetEmptyTest() {
            List<Item> itemFromRepository = itemRepository.search("Отвертка", pageable).toList();
            assertTrue(itemFromRepository.isEmpty());
        }
    }

    @Nested
    @DisplayName("Получить все вещи пользователя")
    class FindAllByOwnerId {
        @Test
        @DisplayName("user1: count: 1, user2: count: 2")
        public void findAllByOwnerIdTest() {

            List<Item> itemsWithPageable = itemRepository.findAllByOwnerId(user2.getId(), pageable).toList();
            List<Item> itemsWithoutPageable = itemRepository.findAllByOwnerId(user1.getId());


            assertEquals(2, itemsWithPageable.size());
            assertEquals(1, itemsWithoutPageable.size());

            Item itemFromRepository1 = itemsWithPageable.get(0);
            Item itemFromRepository2 = itemsWithPageable.get(1);
            Item itemFromRepository3 = itemsWithoutPageable.get(0);

            assertEquals(item2, itemFromRepository1);
            assertEquals(item3, itemFromRepository2);
            assertEquals(item1, itemFromRepository3);
        }

        @Test
        @DisplayName("Негативный тест: Несуществующий пользователь")
        public void shouldGetEmptyTest() {
            List<Item> itemFromRepository = itemRepository.findAllByOwnerId(3L, pageable).toList();
            assertTrue(itemFromRepository.isEmpty());
        }
    }

    @Nested
    @DisplayName("Запрос пользователя на получение вещи по id")
    class FindByIdAndOwnerId {
        @Test
        @DisplayName("Положительный тест")
        public void findByIdAndOwner_IdTest() {
            Optional<Item> itemFromRepository = itemRepository.findByIdAndOwner_Id(item1.getId(), user1.getId());

            assertTrue(itemFromRepository.isPresent());
            assertEquals(item1, itemFromRepository.get());
        }

        @Test
        @DisplayName("Негативный тест: Несуществующая вещь")
        public void shouldGetEmptyTest() {
            Optional<Item> itemFromRepository = itemRepository.findByIdAndOwner_Id(item1.getId(), 3L);
            assertTrue(itemFromRepository.isEmpty());
        }
    }
}