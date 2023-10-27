package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class ItemRequestMapperTest {
    @InjectMocks
    private ItemRequestMapperImpl itemRequestMapper;

    private final LocalDateTime dateTime = LocalDateTime.of(2000, 1, 1, 1, 1, 1);

    private final User user = User.builder()
        .id(1L)
        .name("One")
        .email("one@yandex.ru")
        .build();
    private final List<ItemDto> itemsDto = List.of(ItemDto.builder()
        .id(1L)
        .name("item")
        .description("description")
        .available(true)
        .ownerId(user.getId())
        .requestId(1L)
        .build());
    private final ItemRequest itemRequest = ItemRequest.builder()
        .id(1L)
        .description("itemRequest")
        .requestor(user)
        .created(dateTime)
        .build();
    private final ItemRequestDto itemRequestDto = ItemRequestDto.builder()
        .description("description")
        .build();

    @Nested
    @DisplayName("Маппинг в ItemRequest")
    class ToItemRequest {
        @Test
        public void shouldReturnItemRequest() {
            ItemRequest result = itemRequestMapper.toItemRequest(itemRequestDto, user, dateTime);

            assertEquals(itemRequestDto.getDescription(), result.getDescription());
            assertEquals(user.getName(), result.getRequestor().getName());
            assertEquals(dateTime, result.getCreated());
        }

        @Test
        public void shouldReturnNull() {
            ItemRequest result = itemRequestMapper.toItemRequest(null, null, null);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Маппинг в ItemRequestDto")
    class ToItemRequestDto {
        @Test
        public void shouldReturnItemRequestDto() {
            ItemRequestDto result = itemRequestMapper.toItemRequestDto(itemRequest);

            assertEquals(itemRequest.getId(), result.getId());
            assertEquals(itemRequest.getDescription(), result.getDescription());
            assertEquals(itemRequest.getCreated(), result.getCreated());
        }

        @Test
        public void shouldReturnNull() {
            ItemRequestDto result = itemRequestMapper.toItemRequestDto(null);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Маппинг в ItemRequestResponseDto")
    class ToItemRequestResponseDto {
        @Test
        public void shouldReturnItemRequestExtendedDto() {
            ItemRequestResponseDto result = itemRequestMapper.toItemRequestResponseDto(itemRequest, itemsDto);

            assertEquals(itemRequest.getId(), result.getId());
            assertEquals(itemRequest.getDescription(), result.getDescription());
            assertEquals(itemRequest.getCreated(), result.getCreated());
            assertEquals(itemsDto, result.getItems());
        }

        @Test
        public void shouldReturnNull() {
            ItemRequestResponseDto result = itemRequestMapper.toItemRequestResponseDto(null, null);

            assertNull(result);
        }
    }
}