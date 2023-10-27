package ru.practicum.shareit.request;

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
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestRepositoryTest {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final int from = 0;
    private final int size = 10;
    private final Pageable pageable = PageRequest.of(from / size, size);
    private final LocalDateTime localDateTime = LocalDateTime.of(2000, 1, 1, 1, 1, 1);

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
    private final Item item = Item.builder()
        .id(1L)
        .name("item")
        .description("description")
        .available(true)
        .owner(user1)
        .requestId(1L)
        .build();

    private final ItemRequest itemRequest = ItemRequest.builder()
        .id(1L)
        .description("description")
        .requestor(user2)
        .created(localDateTime)
        .build();

    @BeforeEach
    public void beforeEach() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRequestRepository.save(itemRequest);
        itemRepository.save(item);
    }

    @Nested
    @DisplayName("Получить список запросивших, исключая определенный id")
    class FindByRequestorIdIdNot {

        @Test
        @DisplayName("Получить список, id: 1")
        public void shouldGetOneTest() {
            List<ItemRequest> itemRequests =
                itemRequestRepository.findByRequestorId_IdNot(user1.getId(), pageable).toList();
            assertEquals(1, itemRequests.size());

            ItemRequest itemRequestFromRepository = itemRequests.get(0);
            assertEquals(itemRequest.getId(), itemRequestFromRepository.getId());

        }

        @Test
        @DisplayName("Получить пустой список, id: 2")
        public void shouldGetEmptyTest() {
            List<ItemRequest> itemRequests =
                itemRequestRepository.findByRequestorId_IdNot(user2.getId(), pageable).toList();
            assertTrue(itemRequests.isEmpty());
        }
    }

    @Nested
    @DisplayName("Получить список запросивших, упорядоченный по дате, по возрастанию")
    class FindByRequestorIdIdOrderByCreatedAsc {
        @Test
        @DisplayName("Получить список, id: 2")
        public void shouldGetOneTest() {
            List<ItemRequest> itemsRequest =
                itemRequestRepository.findByRequestorId_IdOrderByCreatedDesc(user2.getId());
            assertEquals(1, itemsRequest.size());

            ItemRequest itemRequestFromRepository = itemsRequest.get(0);
            assertEquals(itemRequest.getId(), itemRequestFromRepository.getId());
        }

        @Test
        @DisplayName("Получить пустой список, id: 1")
        public void shouldGetZeroIfNotRequests() {
            List<ItemRequest> itemsRequest =
                itemRequestRepository.findByRequestorId_IdOrderByCreatedDesc(user1.getId());

            assertTrue(itemsRequest.isEmpty());
        }
    }
}