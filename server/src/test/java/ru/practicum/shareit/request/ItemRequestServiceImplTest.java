package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapperImpl;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @InjectMocks
    ItemRequestServiceImpl itemRequestService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRequestMapperImpl itemRequestMapper;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemMapperImpl itemMapper;
    @Captor
    private ArgumentCaptor<ItemRequest> itemRequestArgumentCaptor;
    private final int from = 0;
    private final int size = 10;
    private final Pageable pageable = PageRequest.of(from / size, size);
    private final LocalDateTime dateTime = LocalDateTime.of(2000, 1, 1, 1, 1, 1);

    private final User user1 = User.builder()
        .id(1L)
        .name("One")
        .email("one@yandex.ru")
        .build();
    private final User user2 = User.builder()
        .id(2L)
        .name("Two")
        .email("two@yandex.ru")
        .build();
    private final Item item1 = Item.builder()
        .id(1L)
        .name("item")
        .description("description")
        .available(true)
        .owner(user1)
        .requestId(1L)
        .build();

    private final ItemDto itemDto = ItemDto.builder()
        .id(item1.getId())
        .name(item1.getName())
        .description(item1.getDescription())
        .available(item1.getAvailable())
        .ownerId(item1.getOwner().getId())
        .requestId(item1.getRequestId())
        .build();

    private final ItemResponseDto itemResponseDto = ItemResponseDto.builder()
        .id(1L)
        .name("item")
        .description("description")
        .available(true)
        .ownerId(user1.getId())
        .requestId(item1.getRequestId())
        .build();
    private final ItemRequest itemRequest1 = ItemRequest.builder()
        .id(1L)
        .description("item description")
        .requestor(user2)
        .created(dateTime)
        .build();

    private final ItemRequestDto itemRequestDto = ItemRequestDto.builder()
        .id(itemRequest1.getId())
        .description(itemRequest1.getDescription())
        .created(itemRequest1.getCreated())
        .build();
    private final ItemRequestResponseDto itemRequestResponseDto = ItemRequestResponseDto.builder()
        .id(itemRequest1.getId())
        .description(itemRequest1.getDescription())
        .created(itemRequest1.getCreated())
        .items(List.of(itemDto))
        .build();

    @Nested
    @DisplayName("Добавление нового запроса")
    class AddItemRequest {
        @Test
        public void shouldAddItemRequest() {
            when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
            when(itemRequestMapper.toItemRequest(eq(itemRequestDto), eq(user2), any())).thenReturn(itemRequest1);
            when(itemRequestRepository.save(any())).thenReturn(itemRequest1);
            when(itemRequestMapper.toItemRequestDto(any())).thenReturn(itemRequestDto);

            ItemRequestDto result = itemRequestService.addItemRequest(itemRequestDto, user2.getId());

            verify(itemRequestRepository, times(1)).save(itemRequestArgumentCaptor.capture());
            verify(itemRequestMapper, times(1)).toItemRequest(any(), any(), any());

            ItemRequest savedItemRequest = itemRequestArgumentCaptor.getValue();
            savedItemRequest.setId(result.getId());

            assertEquals(itemRequest1, savedItemRequest);
            assertEquals(itemRequestDto.getDescription(), savedItemRequest.getDescription());
            assertEquals(user2.getId(), savedItemRequest.getRequestor().getId());
            assertEquals(user2.getName(), savedItemRequest.getRequestor().getName());
            assertEquals(user2.getEmail(), savedItemRequest.getRequestor().getEmail());
            assertNotNull(savedItemRequest.getCreated());
        }

        @Test
        public void shouldValidationException() {
            Long id = 9999L;
            UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> itemRequestService.addItemRequest(itemRequestDto, id));

            assertEquals("Не найден пользователь с id: 9999", exception.getMessage());
            verify(itemRequestRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Получить запросы пользователя")
    class GetByRequestorId {
        @Test
        public void shouldGet() {
            when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
            when(itemRequestRepository.findByRequestorId_IdOrderByCreatedDesc(user2.getId())).thenReturn(
                List.of(itemRequest1));
            when(itemRepository.findByRequestIdIn(List.of(1L))).thenReturn(List.of(item1));
            when(itemMapper.toItemDto(any())).thenCallRealMethod();
            when(itemRequestMapper.toItemRequestResponseDto(any(), any())).thenCallRealMethod();

            List<ItemRequestResponseDto> results = itemRequestService.findAllItemRequestByRequestorId(user2.getId());

            assertEquals(1, results.size());

            ItemRequestResponseDto result = results.get(0);

            assertEquals(itemRequest1.getId(), result.getId());
            assertEquals(itemRequest1.getDescription(), result.getDescription());
            assertEquals(itemRequest1.getCreated(), result.getCreated());
            verify(userRepository, times(1)).findById(user2.getId());
            verify(itemRequestRepository, times(1)).findByRequestorId_IdOrderByCreatedDesc(user2.getId());
            verify(itemRepository, times(1)).findByRequestIdIn(List.of(1L));
            verify(itemMapper, times(1)).toItemDto(any());
            verify(itemRequestMapper, times(1)).toItemRequestResponseDto(any(), any());
        }

        @Test
        public void shouldGetEmpty() {
            when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
            when(itemRequestRepository.findByRequestorId_IdOrderByCreatedDesc(user1.getId()))
                .thenReturn(List.of());

            List<ItemRequestResponseDto> results = itemRequestService.findAllItemRequestByRequestorId(user1.getId());

            assertTrue(results.isEmpty());
            verify(userRepository, times(1)).findById(user1.getId());
            verify(itemRequestRepository, times(1))
                .findByRequestorId_IdOrderByCreatedDesc(user1.getId());
        }

        @Test
        public void shouldValidationException() {
            Long id = 9999L;
            UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> itemRequestService.findAllItemRequestByRequestorId(id));

            assertEquals("Не найден пользователь с id: 9999", exception.getMessage());
            verify(itemRequestRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Получить все запросы")
    class GetAll {
        @Test
        public void shouldGetNotSelfRequests() {
            when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
            when(itemRequestRepository.findByRequestorId_IdNot(user1.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(itemRequest1)));
            when(itemRequestMapper.toItemRequestResponseDto(any(), any())).thenCallRealMethod();

            List<ItemRequestResponseDto> results = itemRequestService.findAllItemRequests(user1.getId(), pageable);


            assertEquals(1, results.size());

            ItemRequestResponseDto result = results.get(0);
            result.setItems(itemRequestResponseDto.getItems());

            assertEquals(itemRequest1.getId(), result.getId());
            assertEquals(itemRequest1.getDescription(), result.getDescription());
            assertEquals(itemRequest1.getCreated(), result.getCreated());
            verify(userRepository, times(1)).findById(user1.getId());
            verify(itemRequestRepository, times(1))
                .findByRequestorId_IdNot(user1.getId(), pageable);
            verify(itemRequestMapper, times(1)).toItemRequestResponseDto(any(), any());
        }

        @Test
        public void shouldGetEmptyIfNotRequests() {
            when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
            when(itemRequestRepository.findByRequestorId_IdNot(user1.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of()));

            List<ItemRequestResponseDto> results = itemRequestService.findAllItemRequests(user1.getId(), pageable);

            assertTrue(results.isEmpty());
            verify(userRepository, times(1)).findById(user1.getId());
            verify(itemRequestRepository, times(1))
                .findByRequestorId_IdNot(user1.getId(), pageable);
        }

        @Test
        public void shouldValidationException() {
            Long id = 9999L;
            UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> itemRequestService.findAllItemRequests(id, pageable));

            assertEquals("Не найден пользователь с id: 9999", exception.getMessage());
            verify(itemRequestRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Получить запрос по id")
    class GetById {
        @Test
        public void shouldGet() {
            when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
            when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest1));
            when(itemRepository.findByRequestId(1L)).thenReturn(List.of(item1));
            when(itemMapper.toItemDto(any())).thenCallRealMethod();
            when(itemRequestMapper.toItemRequestResponseDto(any(), any())).thenCallRealMethod();

            ItemRequestResponseDto result = itemRequestService.findItemRequestById(user2.getId(), 1L);

            assertEquals(itemRequest1.getId(), result.getId());
            assertEquals(itemRequest1.getDescription(), result.getDescription());
            assertEquals(itemRequest1.getCreated(), result.getCreated());
            verify(userRepository, times(1)).findById(user2.getId());
            verify(itemRequestRepository, times(1)).findById(1L);
            verify(itemRepository, times(1)).findByRequestId(1L);
            verify(itemMapper, times(1)).toItemDto(any());
            verify(itemRequestMapper, times(1)).toItemRequestResponseDto(any(), any());
        }

        @Test
        public void shouldThrowExceptionIfItemRequestIdNotFound() {
            when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
            when(itemRequestRepository.findById(1L)).thenReturn(Optional.empty());

            UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> itemRequestService.findItemRequestById(user2.getId(), 1L));
            assertEquals("Не найден запрос с id: 1", exception.getMessage());
            verify(userRepository, times(1)).findById(user2.getId());
            verify(itemRequestRepository, times(1)).findById(1L);
        }

        @Test
        public void shouldValidationException() {
            Long id = 9999L;
            UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> itemRequestService.findItemRequestById(id, itemRequest1.getId()));

            assertEquals("Не найден пользователь с id: 9999", exception.getMessage());
            verify(itemRequestRepository, never()).save(any());
        }
    }
}