package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemResponseDto;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestControllerTest {
    @InjectMocks
    private ItemRequestController itemRequestController;
    @Mock
    private ItemRequestService itemRequestService;

    private final LocalDateTime localDateTime = LocalDateTime.of(2000, 1, 1, 1, 1, 1);

    private int from;
    private int size;
    private final UserDto userDto1 = UserDto.builder()
        .id(1L)
        .name("One")
        .email("one@yandex.ru")
        .build();

    private final UserDto userDto2 = UserDto.builder()
        .id(2L)
        .name("Two")
        .email("two@yandex.ru")
        .build();

    private final ItemResponseDto item = ItemResponseDto.builder()
        .id(1L)
        .name("item")
        .description("description")
        .available(true)
        .ownerId(userDto1.getId())
        .requestId(1L)
        .build();

    private final ItemDto itemDto = ItemDto.builder()
        .id(item.getId())
        .name(item.getName())
        .description(item.getDescription())
        .available(item.getAvailable())
        .ownerId(item.getOwnerId())
        .requestId(item.getRequestId())
        .build();

    private final ItemRequestDto itemRequestAddDto = ItemRequestDto.builder()
        .description("description")
        .created(localDateTime)
        .build();

    private final ItemRequestDto itemRequestDto = ItemRequestDto.builder()
        .id(1L)
        .description("description")
        .requestor(userDto2.getId())
        .created(localDateTime)
        .build();

    private final ItemRequestResponseDto itemRequestResponseDto = ItemRequestResponseDto.builder()
        .id(itemRequestDto.getId())
        .description(itemRequestDto.getDescription())
        .created(itemRequestDto.getCreated())
        .items(List.of(itemDto))
        .build();

    @BeforeEach
    public void beforeEach() {
        from = 0;
        size = 10;
    }

    @Nested
    @DisplayName("POST")
    public class MethodPost {
        @Test
        @DisplayName("Создание запроса")
        public void methodPost_createItemRequestTest() {
            when(itemRequestService.addItemRequest(itemRequestAddDto, userDto2.getId()))
                .thenReturn(itemRequestDto);

            ResponseEntity<ItemRequestDto> response = itemRequestController.addItemRequest(
                userDto2.getId(),
                itemRequestAddDto);

            assertEquals(OK, response.getStatusCode());
            assertEquals(Objects.requireNonNull(response.getBody()).getId(),
                itemRequestDto.getId());
            assertEquals(Objects.requireNonNull(response.getBody()).getDescription(),
                itemRequestDto.getDescription());
            assertEquals(Objects.requireNonNull(response.getBody()).getCreated(),
                itemRequestDto.getCreated());
            assertEquals(Objects.requireNonNull(response.getBody()).getRequestor(),
                itemRequestDto.getRequestor());

        }

        @Test
        @DisplayName("Создание запроса - description : null")
        public void methodPost_createItemEmptyDescriptionTest() {
            itemRequestAddDto.setDescription(null);

            when(itemRequestService.addItemRequest(itemRequestAddDto, userDto2.getId()))
                .thenThrow(ValidationException.class);

            assertThrows(ValidationException.class, () -> {
                itemRequestController.addItemRequest(userDto2.getId(), itemRequestAddDto);
            });
        }
    }

    @Nested
    @DisplayName("GET")
    public class MethodGet {
        @Test
        @DisplayName("Получение запросов пользователя")
        public void methodGet_ItemRequestByRequestorTest() {
            when(itemRequestService.findAllItemRequestByRequestorId(userDto2.getId()))
                .thenReturn(List.of(itemRequestResponseDto));

            ResponseEntity<List<ItemRequestResponseDto>> response =
                itemRequestController.getItemRequestByRequestor(userDto2.getId());

            assertEquals(OK, response.getStatusCode());
            assertEquals(1, Objects.requireNonNull(response.getBody()).size());
        }

        @Test
        @DisplayName("Получение запросов пользователя - user id: 9999")
        public void methodGet_ItemRequestByRequestorNotFoundTest() {
            when(itemRequestService.findAllItemRequestByRequestorId(9999L))
                .thenThrow(UserNotFoundException.class);
            assertThrows(UserNotFoundException.class, () -> {
                itemRequestController.getItemRequestByRequestor(9999L);
            });
        }

        @Test
        @DisplayName("Получение всех запросов пользователем")
        public void methodGet_AllItemRequestsTest() {
            Pageable pageable = PageRequest.of(from / size, size);

            when(itemRequestService.findAllItemRequests(userDto2.getId(), pageable))
                .thenReturn(List.of(itemRequestResponseDto));

            ResponseEntity<List<ItemRequestResponseDto>> response =
                itemRequestController.getAllItemRequests(userDto2.getId(), from, size);

            assertEquals(OK, response.getStatusCode());
            assertEquals(1, Objects.requireNonNull(response.getBody()).size());
        }

        @Test
        @DisplayName("Получение запроса с id: 1, пользователем: userDto2")
        public void methodGet_ItemRequestById() {
            when(itemRequestService.findItemRequestById(userDto2.getId(), itemRequestDto.getId()))
                .thenReturn(itemRequestResponseDto);

            ResponseEntity<ItemRequestResponseDto> response =
                itemRequestController.getItemRequestById(userDto2.getId(), itemRequestDto.getId());

            assertEquals(OK, response.getStatusCode());
            assertEquals(Objects.requireNonNull(response.getBody()).getId(),
                itemRequestDto.getId());
            assertEquals(Objects.requireNonNull(response.getBody()).getDescription(),
                itemRequestDto.getDescription());
            assertEquals(Objects.requireNonNull(response.getBody()).getCreated(),
                itemRequestDto.getCreated());
        }

        @Test
        @DisplayName("Получение запроса с id: 1, пользователем с id: 9999")
        public void methodGet_ItemRequestByIdNotFoundTest() {
            when(itemRequestService.findItemRequestById(9999L, itemRequestDto.getId()))
                .thenThrow(UserNotFoundException.class);
            assertThrows(UserNotFoundException.class, () -> {
                itemRequestController.getItemRequestById(9999L, itemRequestDto.getId());
            });
        }
    }
}