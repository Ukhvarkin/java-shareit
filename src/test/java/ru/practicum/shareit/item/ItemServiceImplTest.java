package ru.practicum.shareit.item;

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
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.booking.BookingStatus.APPROVED;
import static ru.practicum.shareit.booking.BookingStatus.REJECTED;
import static ru.practicum.shareit.booking.BookingStatus.WAITING;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemMapperImpl itemMapper;
    @Mock
    private CommentMapper commentMapper;
    @InjectMocks
    private ItemServiceImpl itemService;
    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;
    @Captor
    private ArgumentCaptor<Comment> commentArgumentCaptor;
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
        .build();

    private final ItemResponseDto item1Dto = ItemResponseDto.builder()
        .id(1L)
        .name("item")
        .description("description")
        .available(true)
        .ownerId(user1.getId())
        .build();
    Comment comment = Comment.builder()
        .id(1L)
        .text("text")
        .created(dateTime.plusYears(5))
        .author(user2)
        .item(item1)
        .build();

    CommentDto commentDto = CommentDto.builder()
        .id(1L)
        .text("text")
        .created(dateTime.plusYears(5))
        .authorName(user2.getName())
        .build();

    private final Booking booking1 = Booking.builder()
        .id(1L)
        .start(dateTime.plusMinutes(1))
        .end(dateTime.plusYears(10))
        .item(item1)
        .booker(user2)
        .status(APPROVED)
        .build();
    private final Booking booking2 = Booking.builder()
        .id(2L)
        .start(dateTime.minusYears(5))
        .end(dateTime.plusYears(5))
        .item(item1)
        .booker(user2)
        .status(APPROVED)
        .build();
    private final Booking booking3 = Booking.builder()
        .id(3L)
        .start(dateTime.plusYears(8))
        .end(dateTime.plusYears(9))
        .item(item1)
        .booker(user2)
        .status(WAITING)
        .build();
    private final Booking booking4 = Booking.builder()
        .id(4L)
        .start(dateTime.plusYears(9))
        .end(dateTime.plusYears(10))
        .item(item1)
        .booker(user2)
        .status(REJECTED)
        .build();


    private void userRepositoryWhen(User userWhen) {
        when(userRepository.findById(userWhen.getId())).thenReturn(Optional.of(userWhen));
    }

    @Nested
    @DisplayName("Добавление новой вещи")
    class AddItemTest {
        @Test
        public void shouldAdd() {
            userRepositoryWhen(user1);
            when(itemMapper.toItem(any(), any(), any())).thenCallRealMethod();

            itemService.addItem(item1Dto, user1.getId());

            verify(userRepository, times(1)).findById(user1.getId());
            verify(itemRepository, times(1)).save(item1);
            verify(itemMapper, times(1)).toItem(any(), any(), any());
            verify(itemMapper, times(1)).toItemResponseDto(any(), any(), any(), any());
        }

        @Test
        public void shouldValidationException() {

            ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addItem(null, user1.getId()));

            assertEquals("Некорректный ввод. Пустой объект.", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldValidationException_NameIsNull() {
            ItemResponseDto itemDtoInValid = ItemResponseDto.builder()
                .id(1L)
                .name(null)
                .description("description")
                .available(true)
                .ownerId(user1.getId())
                .build();

            ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addItem(itemDtoInValid, user1.getId()));

            assertEquals("Некорректный ввод, пустое поле имени.", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldValidationException_EmptyName() {
            ItemResponseDto itemDtoInValid = ItemResponseDto.builder()
                .id(1L)
                .name("")
                .description("description")
                .available(true)
                .ownerId(user1.getId())
                .build();

            ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addItem(itemDtoInValid, user1.getId()));

            assertEquals("Некорректный ввод, пустое поле имени.", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldValidationException_DescriptionIsNull() {
            ItemResponseDto itemDtoInValid = ItemResponseDto.builder()
                .id(1L)
                .name("item")
                .description(null)
                .available(true)
                .ownerId(user1.getId())
                .build();

            ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addItem(itemDtoInValid, user1.getId()));

            assertEquals("Некорректный ввод, пустое поле описания.", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldValidationException_AvailableIsNull() {
            ItemResponseDto itemDtoInValid = ItemResponseDto.builder()
                .id(1L)
                .name("item")
                .description("description")
                .available(null)
                .ownerId(user1.getId())
                .build();

            ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addItem(itemDtoInValid, user1.getId()));

            assertEquals("Некорректный ввод, пустое поле наличия вещи.", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Редактирование вещи")
    class Update {

        @Test
        public void shouldUpdateItemByOwner() {
            ItemResponseDto updatedItemDto = ItemResponseDto.builder()
                .id(1L)
                .name("Updated Name")
                .description("Updated Description")
                .available(false)
                .ownerId(user1.getId())
                .comments(List.of(commentDto))
                .build();

            when(itemRepository.findByIdAndOwner_Id(item1.getId(), user1.getId())).thenReturn(Optional.of(item1));

            itemService.updateItem(updatedItemDto, item1.getId());

            verify(itemRepository, times(1)).findByIdAndOwner_Id(any(), any());
            verify(itemRepository, times(1)).save(itemArgumentCaptor.capture());

            Item savedItem = itemArgumentCaptor.getValue();

            assertEquals(item1.getId(), savedItem.getId());
            assertEquals(updatedItemDto.getName(), savedItem.getName());
            assertEquals(updatedItemDto.getDescription(), savedItem.getDescription());
            assertEquals(updatedItemDto.getAvailable(), savedItem.getAvailable());
        }

        @Test
        public void shouldNotUpdateIfNull() {
            ItemResponseDto updatedItemDto = ItemResponseDto.builder()
                .id(1L)
                .name(null)
                .description(null)
                .available(false)
                .ownerId(user1.getId())
                .comments(List.of(commentDto))
                .build();

            when(itemRepository.findByIdAndOwner_Id(item1.getId(), user1.getId())).thenReturn(Optional.of(item1));

            itemService.updateItem(updatedItemDto, user1.getId());

            verify(itemRepository, times(1)).findByIdAndOwner_Id(any(), any());
            verify(itemRepository, times(1)).save(itemArgumentCaptor.capture());

            Item savedItem = itemArgumentCaptor.getValue();

            assertEquals(item1.getId(), savedItem.getId());
            assertEquals(item1.getName(), savedItem.getName());
            assertEquals(item1.getDescription(), savedItem.getDescription());
            assertEquals(item1.getAvailable(), savedItem.getAvailable());
        }

        @Test
        public void shouldThrowExceptionIfUpdateByNotOwner() {
            Long userId = 9999L;
            when(itemRepository.findByIdAndOwner_Id(item1Dto.getId(), userId)).thenReturn(Optional.empty());

            ItemNotFoundException exception = assertThrows(ItemNotFoundException.class, () ->
                itemService.updateItem(item1Dto, userId));

            assertEquals("Вещь с id: " + item1Dto.getId() + " не найдена для пользователя с id: " + userId,
                exception.getMessage());
            verify(itemRepository, times(1)).findByIdAndOwner_Id(item1Dto.getId(), userId);
        }
    }

    @Nested
    @DisplayName("Получить вещь по id")
    class GetById {
        @Test
        public void shouldGetItemById() {
            when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
            when(itemMapper.toItemResponseDto(any(), any(), any(), any())).thenCallRealMethod();

            ItemResponseDto itemFromService = itemService.getItemById(item1.getId(), user2.getId());

            assertNull(itemFromService.getLastBooking());
            assertNull(itemFromService.getNextBooking());
            verify(itemRepository, times(1)).findById(any());
            verify(itemMapper, times(1)).toItemResponseDto(any(), any(), any(), any());
        }

        @Test
        public void shouldThrow_ItemNotFoundException() {
            Long id = 9999L;
            when(itemRepository.findById(id)).thenReturn(Optional.empty());

            ItemNotFoundException exception = assertThrows(ItemNotFoundException.class,
                () -> itemService.getItemById(id, user1.getId()));

            assertEquals("Не существует вещи с id: 9999", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldGetByOwnerWithBookings() {
            when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
            when(itemMapper.toItemResponseDto(any(), any(), any(), any())).thenCallRealMethod();
            when(bookingRepository.findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(any(), any(), any()))
                .thenReturn(List.of(booking2, booking1));
            when(bookingRepository.findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(any(), any(), any()))
                .thenReturn(List.of(booking3, booking4));
            when(itemMapper.toBookingItemDto(any())).thenCallRealMethod();

            ItemResponseDto itemFromService = itemService.getItemById(item1.getId(), user1.getId());

            assertNotNull(itemFromService.getLastBooking());
            assertEquals(booking2.getId(), itemFromService.getLastBooking().getId());
            assertEquals(booking2.getBooker().getId(), itemFromService.getLastBooking().getBookerId());
            assertEquals(booking2.getStart(), itemFromService.getLastBooking().getStart());
            assertEquals(booking2.getEnd(), itemFromService.getLastBooking().getEnd());

            assertNotNull(itemFromService.getNextBooking());
            assertEquals(booking3.getId(), itemFromService.getNextBooking().getId());
            assertEquals(booking3.getBooker().getId(), itemFromService.getNextBooking().getBookerId());
            assertEquals(booking3.getStart(), itemFromService.getNextBooking().getStart());
            assertEquals(booking3.getEnd(), itemFromService.getNextBooking().getEnd());

            verify(itemRepository, times(1)).findById(any());
            verify(itemMapper, times(1)).toItemResponseDto(any(), any(), any(), any());
            verify(bookingRepository, times(1))
                .findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(any(), any(), any());
            verify(bookingRepository, times(1))
                .findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(any(), any(), any());
            verify(itemMapper, times(2)).toBookingItemDto(any());
        }

        @Test
        public void shouldGetByOwnerWithEmptyLastAndNextBookings() {
            when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
            when(itemMapper.toItemResponseDto(any(), any(), any(), any())).thenCallRealMethod();
            when(bookingRepository.findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(any(), any(), any()))
                .thenReturn(List.of());
            when(bookingRepository.findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(any(), any(), any()))
                .thenReturn(List.of());

            ItemResponseDto itemFromService = itemService.getItemById(user1.getId(), item1.getId());

            assertNull(itemFromService.getLastBooking());
            assertNull(itemFromService.getNextBooking());

            verify(itemRepository, times(1)).findById(any());
            verify(itemMapper, times(1)).toItemResponseDto(any(), any(), any(), any());
            verify(bookingRepository, times(1))
                .findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(any(), any(), any());
            verify(bookingRepository, times(1))
                .findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(any(), any(), any());
            verify(itemMapper, never()).toBookingItemDto(any());
        }
    }

    @Nested
    @DisplayName("Получить вещи владельца")
    class GetItemByOwnerId {
        @Test
        public void shouldGetTwoItems() {
            when(userRepository.findById(any())).thenReturn(Optional.of(user1));
            when(itemRepository.findAllByOwnerId(any(), any())).thenReturn(new PageImpl<>(List.of(item1)));
            when(itemMapper.toItemResponseDto(any(), any(), any(), any())).thenCallRealMethod();

            itemService.findAllItemsByOwner(user1.getId(), from, size);

            verify(itemRepository, times(1)).findAllByOwnerId(eq(user1.getId()), any());
            verify(itemMapper, times(1)).toItemResponseDto(any(), any(), any(), any());
        }

        @Test
        public void shouldGetEmptyItems() {
            when(userRepository.findById(any())).thenReturn(Optional.of(user1));
            when(itemRepository.findAllByOwnerId(any(), any())).thenReturn(new PageImpl<>(List.of()));

            itemService.findAllItemsByOwner(user1.getId(), from, size);

            verify(itemRepository, times(1)).findAllByOwnerId(any(), any());
            verify(itemMapper, never()).toItemResponseDto(any(), any(), any(), any());
        }

        @Test
        public void shouldThrow_ItemNotFoundException() {
            Long id = 9999L;
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> itemService.findAllItemsByOwner(id, from, size));

            assertEquals("Не найден пользователь с id: 9999", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Поиск по названию или описанию")
    class Search {
        @Test
        public void shouldGet() {
            when(itemRepository.search("EM", pageable)).thenReturn(new PageImpl<>(List.of(item1)));

            List<ItemResponseDto> itemsFromService = itemService.search("EM", pageable);

            assertEquals(1, itemsFromService.size());
            verify(itemRepository, times(1)).search(eq("EM"), eq(pageable));
        }

        @Test
        public void shouldGetEmptyList() {
            List<ItemResponseDto> itemsFromService = itemService.search("", pageable);

            assertTrue(itemsFromService.isEmpty());
            verify(itemRepository, never()).search(any(), any());
        }

        @Test
        public void shouldGetEmptyListIfNull() {
            List<ItemResponseDto> itemsFromService = itemService.search(null, pageable);

            assertTrue(itemsFromService.isEmpty());
            verify(itemRepository, never()).search(any(), any());
        }
    }

    @Nested
    @DisplayName("Добавление нового комментария")
    class AddComment {
        @Test
        public void shouldAdd() {
            when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
            when(bookingRepository.countAllByItemIdAndBookerIdAndEndBefore(eq(item1.getId()), eq(user2.getId()), any()))
                .thenReturn(1L);
            when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
            when(commentMapper.toComment(commentDto, item1, user2)).thenReturn(comment);

            when(commentRepository.save(comment)).thenReturn(comment);

            CommentDto newComment = itemService.addComment(item1.getId(), commentDto, user2.getId());

            verify(userRepository, times(1)).findById(user2.getId());
            verify(bookingRepository, times(1))
                .countAllByItemIdAndBookerIdAndEndBefore(eq(item1.getId()), eq(user2.getId()), any());
            verify(commentRepository, times(1)).save(commentArgumentCaptor.capture());

            Comment savedComment = commentArgumentCaptor.getValue();
            assertEquals(comment, savedComment);
        }

        @Test
        public void shouldThrow_ValidationException() {
            Long id = 9999L;

            when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
            when(bookingRepository.countAllByItemIdAndBookerIdAndEndBefore(eq(id), eq(user1.getId()), any()))
                .thenReturn(null);

            ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addComment(id, commentDto, user1.getId()));

            assertEquals("Вы не брали эту вещь в аренду.", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldThrow_UserNotFoundException() {
            Long id = 9999L;

            when(userRepository.findById(id)).thenReturn(Optional.empty());

            UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> itemService.addComment(item1.getId(), commentDto, id));

            assertEquals("Не существует пользователя с id: 9999", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldThrow_ItemNotFoundException() {
            Long id = 9999L;
            when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
            when(bookingRepository.countAllByItemIdAndBookerIdAndEndBefore(eq(id), eq(user2.getId()), any()))
                .thenReturn(1L);

            ItemNotFoundException exception = assertThrows(ItemNotFoundException.class,
                () -> itemService.addComment(id, commentDto, user2.getId()));

            assertEquals("Не найдена вещь с id: 9999", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

    }

}