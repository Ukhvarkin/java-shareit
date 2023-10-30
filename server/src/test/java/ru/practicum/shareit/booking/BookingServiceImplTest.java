package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.booking.BookingState.ALL;
import static ru.practicum.shareit.booking.BookingState.CURRENT;
import static ru.practicum.shareit.booking.BookingState.FUTURE;
import static ru.practicum.shareit.booking.BookingState.PAST;
import static ru.practicum.shareit.booking.BookingStatus.APPROVED;
import static ru.practicum.shareit.booking.BookingStatus.REJECTED;
import static ru.practicum.shareit.booking.BookingStatus.WAITING;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Captor
    private ArgumentCaptor<Booking> bookingArgumentCaptor;
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
    private final User user3 = User.builder()
        .id(3L)
        .name("Three")
        .email("three@yandex.ru")
        .build();

    private final Item item1 = Item.builder()
        .id(1L)
        .name("item")
        .description("description")
        .available(true)
        .owner(user1)
        .build();
    private final BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
        .start(dateTime.plusYears(30))
        .end(dateTime.plusYears(40))
        .itemId(item1.getId())
        .build();

    private final Booking booking = Booking.builder()
        .id(1L)
        .start(dateTime.plusYears(30))
        .end(dateTime.plusYears(40))
        .item(item1)
        .booker(user2)
        .status(APPROVED)
        .build();

    private final UserDto user2Dto = UserDto.builder()
        .id(2L)
        .name("Two")
        .email("two@yandex.ru")
        .build();

    private final ItemResponseDto item1Dto = ItemResponseDto.builder()
        .id(1L)
        .name("item")
        .description("description")
        .available(true)
        .ownerId(user1.getId())
        .build();

    private final BookingResponseDto bookingResponseDto = BookingResponseDto.builder()
        .id(1L)
        .start(dateTime.plusYears(30))
        .end(dateTime.plusYears(40))
        .item(item1Dto)
        .booker(user2Dto)
        .status(APPROVED)
        .build();

    private void userRepositoryWhen(User userWhen) {
        when(userRepository.findById(userWhen.getId())).thenReturn(Optional.of(userWhen));
    }

    private void equalsChecker(Booking booking, BookingResponseDto bookingResponseDto) {
        assertEquals(booking.getId(), bookingResponseDto.getId());
        assertEquals(booking.getStart(), bookingResponseDto.getStart());
        assertEquals(booking.getEnd(), bookingResponseDto.getEnd());
        assertEquals(booking.getBooker().getId(), bookingResponseDto.getBooker().getId());
        assertEquals(booking.getBooker().getName(), bookingResponseDto.getBooker().getName());
        assertEquals(booking.getBooker().getEmail(), bookingResponseDto.getBooker().getEmail());
        assertEquals(booking.getStatus(), bookingResponseDto.getStatus());
        assertEquals(booking.getItem().getId(), bookingResponseDto.getItem().getId());
        assertEquals(booking.getItem().getName(), bookingResponseDto.getItem().getName());
        assertEquals(booking.getItem().getDescription(), bookingResponseDto.getItem().getDescription());
        assertEquals(booking.getItem().getAvailable(), bookingResponseDto.getItem().getAvailable());
        assertEquals(booking.getItem().getRequestId(), bookingResponseDto.getItem().getRequestId());
        assertEquals(booking.getItem().getOwner().getId(), bookingResponseDto.getItem().getOwnerId());
    }

    @Nested
    class CreateBookingTest {
        @Test
        void shouldCreatedBooking() {
            when(itemRepository.findById(bookingRequestDto.getItemId())).thenReturn(Optional.of(item1));
            userRepositoryWhen(user2);
            when(bookingMapper.toBooking(bookingRequestDto, item1, user2, WAITING)).thenReturn(booking);

            bookingService.addBooking(bookingRequestDto, user2.getId());

            verify(itemRepository, times(1)).findById(bookingRequestDto.getItemId());
            verify(userRepository, times(1)).findById(user2.getId());
            verify(bookingMapper, times(1))
                .toBooking(bookingRequestDto, item1, user2, WAITING);
            verify(bookingRepository, times(1)).save(bookingArgumentCaptor.capture());

            Booking savedBooking = bookingArgumentCaptor.getValue();

            assertEquals(booking, savedBooking);
            assertEquals(booking.getId(), savedBooking.getId());
            assertEquals(booking.getStatus(), savedBooking.getStatus());
            assertEquals(booking.getStart(), savedBooking.getStart());
            assertEquals(booking.getEnd(), savedBooking.getEnd());
            assertEquals(booking.getBooker().getId(), savedBooking.getBooker().getId());
        }

        @Test
        public void shouldThrowException_ItemIdNull() {
            BookingRequestDto bookingWrongDate = BookingRequestDto.builder()
                .start(dateTime.plusYears(30))
                .end(dateTime.plusYears(40))
                .itemId(null)
                .build();

            ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.addBooking(bookingWrongDate, user2.getId()));
            assertEquals("Не указан идентификатор вещи (itemId).", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldValidationException_StartIsNull() {
            BookingRequestDto bookingWrongDate = BookingRequestDto.builder()
                .start(null)
                .end(dateTime.plusYears(40))
                .itemId(item1.getId())
                .build();

            ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.addBooking(bookingWrongDate, user2.getId()));
            assertEquals("Введите время.", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldValidationException_EndIsNull() {
            BookingRequestDto bookingWrongDate = BookingRequestDto.builder()
                .start(dateTime.plusYears(30))
                .end(null)
                .itemId(item1.getId())
                .build();

            ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.addBooking(bookingWrongDate, user2.getId()));
            assertEquals("Введите время.", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldValidationException_StartInPast() {
            BookingRequestDto bookingWrongDate = BookingRequestDto.builder()
                .start(dateTime.minusYears(40))
                .end(dateTime.plusYears(40))
                .itemId(item1.getId())
                .build();

            ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.addBooking(bookingWrongDate, user2.getId()));
            assertEquals("Время старта не может быть в прошлом.", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldValidationException_EndInPast() {
            BookingRequestDto bookingWrongDate = BookingRequestDto.builder()
                .start(dateTime.plusYears(30))
                .end(dateTime.minusYears(40))
                .itemId(item1.getId())
                .build();

            ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.addBooking(bookingWrongDate, user2.getId()));
            assertEquals("Время окончания не может быть в прошлом.", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldValidationException_EndBeforeStart() {
            BookingRequestDto bookingWrongDate = BookingRequestDto.builder()
                .start(dateTime.plusYears(30))
                .end(dateTime.plusYears(30))
                .itemId(item1.getId())
                .build();

            ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.addBooking(bookingWrongDate, user2.getId()));
            assertEquals("Время старта не может быть равно окончанию.", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }


        @Test
        public void shouldThrowException_ItemIsNotAvailable() {
            Item itemIsNoAvailable = Item.builder()
                .id(1L)
                .name("item")
                .description("description")
                .available(false)
                .owner(user1)
                .build();

            BookingRequestDto bookingInValid = BookingRequestDto.builder()
                .start(dateTime.plusYears(30))
                .end(dateTime.plusYears(40))
                .itemId(itemIsNoAvailable.getId())
                .build();

            when(itemRepository.findById(bookingRequestDto.getItemId())).thenReturn(Optional.of(itemIsNoAvailable));

            ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.addBooking(bookingInValid, user2.getId()));
            assertEquals("Бронь недоступна.", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldThrow_ItemNotFoundException() {
            BookingRequestDto bookingInValid = BookingRequestDto.builder()
                .start(dateTime.plusYears(30))
                .end(dateTime.plusYears(40))
                .itemId(9999L)
                .build();

            when(itemRepository.findById(9999L)).thenReturn(Optional.empty());

            ItemNotFoundException exception = assertThrows(ItemNotFoundException.class,
                () -> bookingService.addBooking(bookingInValid, user2.getId()));
            assertEquals("Не найдена вещь с id: 9999", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldThrow_UserNotFoundException() {
            when(itemRepository.findById(bookingRequestDto.getItemId())).thenReturn(Optional.of(item1));
            when(userRepository.findById(9999L)).thenReturn(Optional.empty());

            UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> bookingService.addBooking(bookingRequestDto, 9999L));

            assertEquals("Не найден пользователь с id: 9999 не существует.", exception.getMessage());
            verify(itemRepository, times(1)).findById(bookingRequestDto.getItemId());
            verify(userRepository, times(1)).findById(9999L);
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldThrowExceptionIfBookingByOwner() {
            when(itemRepository.findById(bookingRequestDto.getItemId())).thenReturn(Optional.of(item1));
            userRepositoryWhen(user1);

            ItemNotFoundException exception = assertThrows(ItemNotFoundException.class,
                () -> bookingService.addBooking(bookingRequestDto, user1.getId()));
            assertEquals("Вы не можете забронировать свою вещь", exception.getMessage());
            verify(itemRepository, times(1)).findById(bookingRequestDto.getItemId());
            verify(userRepository, times(1)).findById(user1.getId());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldThrowExceptionIfEndIsBeforeStart() {
            BookingRequestDto bookingWrongDate = BookingRequestDto.builder()
                .start(dateTime.plusYears(0))
                .end(dateTime.plusYears(40))
                .itemId(item1.getId())
                .build();

            ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.addBooking(bookingWrongDate, user2.getId()));
            assertEquals("Время старта не может быть в прошлом.", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldThrowBookingNotFoundExceptionWhenOverlappingBookingsExist() {
            LocalDateTime start = dateTime.plusYears(30);
            LocalDateTime end = dateTime.plusYears(40);

            when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
            userRepositoryWhen(user2);

            when(bookingRepository.findBookingsByItemIdAndTimeRangeAndStatus(
                item1.getId(), start, end, APPROVED)).thenReturn(Collections.singletonList(booking));

            BookingRequestDto bookingTimeRequestDto = BookingRequestDto.builder()
                .start(start)
                .end(end)
                .itemId(item1.getId())
                .build();

            BookingNotFoundException exception = assertThrows(BookingNotFoundException.class,
                () -> bookingService.addBooking(bookingTimeRequestDto, user2.getId()));

            assertEquals("В данный момент бронирование не доступно.", exception.getMessage());
        }
    }

    @Nested
    class UpdateBooking {
        Booking bookingIsWaiting1 = Booking.builder()
            .id(3L)
            .start(dateTime.plusYears(8))
            .end(dateTime.plusYears(9))
            .item(item1)
            .booker(user2)
            .status(WAITING)
            .build();

        @Test
        public void shouldApprove() {

            userRepositoryWhen(user1);
            when(bookingRepository.findById(bookingIsWaiting1.getId())).thenReturn(Optional.of(bookingIsWaiting1));

            bookingService.approveBooking(bookingIsWaiting1.getId(), user1.getId(), true);

            verify(bookingRepository, times(1)).findById(bookingIsWaiting1.getId());
            verify(bookingRepository, times(1)).save(bookingArgumentCaptor.capture());

            Booking savedBooking = bookingArgumentCaptor.getValue();

            assertEquals(APPROVED, savedBooking.getStatus());
        }

        @Test
        public void shouldReject() {
            userRepositoryWhen(user1);
            when(bookingRepository.findById(bookingIsWaiting1.getId())).thenReturn(Optional.of(bookingIsWaiting1));

            bookingService.approveBooking(bookingIsWaiting1.getId(), user1.getId(), false);

            verify(bookingRepository, times(1)).findById(bookingIsWaiting1.getId());
            verify(bookingRepository, times(1)).save(bookingArgumentCaptor.capture());

            Booking savedBooking = bookingArgumentCaptor.getValue();

            assertEquals(REJECTED, savedBooking.getStatus());
            verify(userRepository, times(1)).findById(user1.getId());
        }

        @Test
        public void shouldThrow_UserNotFoundException() {
            when(userRepository.findById(9999L)).thenReturn(Optional.empty());

            UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> bookingService.approveBooking(bookingIsWaiting1.getId(), 9999L, true));

            assertEquals("Не найден пользователь с id: 9999 не существует.", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldThrow_BookingNotFoundException() {
            userRepositoryWhen(user1);
            when(bookingRepository.findById(9999L)).thenReturn(Optional.empty());

            BookingNotFoundException exception = assertThrows(BookingNotFoundException.class,
                () -> bookingService.approveBooking(9999L, user1.getId(), true));

            assertEquals("Нет бронирования с id: 9999", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldThrow_UserNotFoundExceptionForNonOwner() {
            when(bookingRepository.findById(bookingIsWaiting1.getId())).thenReturn(Optional.of(bookingIsWaiting1));
            userRepositoryWhen(user2);

            UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> bookingService.approveBooking(bookingIsWaiting1.getId(), user2.getId(), true));

            assertEquals("Только владелец может подтверждать или отклонять бронирование.", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }

        @Test
        public void shouldThrow_ValidationExceptionForAlreadyApproved() {
            bookingIsWaiting1.setStatus(APPROVED);
            when(bookingRepository.findById(bookingIsWaiting1.getId())).thenReturn(Optional.of(bookingIsWaiting1));
            userRepositoryWhen(user1);

            ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.approveBooking(bookingIsWaiting1.getId(), user1.getId(), true));

            assertEquals("Бронирование уже подтверждено.", exception.getMessage());
            verify(bookingRepository, never()).save(any());
        }
    }

    @Nested
    class FindBookingByIdTest {

        @Test
        public void shouldGetByAuthor() {
            userRepositoryWhen(user2);
            when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
            when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            BookingResponseDto response = bookingService.findBookingById(booking.getId(), user2.getId());

            equalsChecker(booking, response);
            verify(userRepository, times(1)).findById(user2.getId());
            verify(bookingRepository, times(1)).findById(booking.getId());
            verify(bookingMapper, times(1)).toBookingResponseDto(booking);
        }

        @Test
        public void shouldGetByOwner() {
            userRepositoryWhen(user1);
            when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
            when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            BookingResponseDto response = bookingService.findBookingById(user1.getId(), booking.getId());

            equalsChecker(booking, response);
            verify(bookingRepository, times(1)).findById(booking.getId());
            verify(bookingMapper, times(1)).toBookingResponseDto(booking);
        }

        @Test
        public void shouldThrow_BookingNotFoundException() {
            userRepositoryWhen(user1);
            when(bookingRepository.findById(9999L)).thenReturn(Optional.empty());

            BookingNotFoundException exception = assertThrows(BookingNotFoundException.class,
                () -> bookingService.findBookingById(9999L, user1.getId()));

            assertEquals("Не существует бронирования с id: 9999", exception.getMessage());
            verify(bookingRepository, times(1)).findById(9999L);
            verify(userRepository, times(1)).findById(user1.getId());
        }

        @Test
        public void shouldThrow_UserNotFoundException_NotOwnerAndAuthor() {
            userRepositoryWhen(user3);
            when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

            UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> bookingService.findBookingById(booking.getId(), user3.getId()));
            assertEquals("Просмотр бронирования доступно только автору или владельцу.", exception.getMessage());
            verify(bookingRepository, times(1)).findById(booking.getId());
            verify(userRepository, times(1)).findById(user3.getId());
        }
    }

    @Nested
    class FindAllBookingByUserId {
        @Test
        public void shouldGetAllByUser() {
            userRepositoryWhen(user2);
            when(bookingRepository.findByBookerId(user2.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(booking)));

            when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> results = bookingService.findAllBookingByUserId(user2.getId(), ALL, pageable);

            assertEquals(1, results.size());
            BookingResponseDto response = results.get(0);

            equalsChecker(booking, response);
            verify(bookingRepository, times(1))
                .findByBookerId(user2.getId(), pageable);
            verify(bookingMapper, times(1)).toBookingResponseDto(booking);
            verify(userRepository, times(1)).findById(user2.getId());
        }

        @Test
        public void shouldGetCurrentByUser() {
            userRepositoryWhen(user2);
            when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> response = bookingService.findAllBookingByUserId(user2.getId(), CURRENT, pageable);

            assertEquals(1, response.size());

            BookingResponseDto result = response.get(0);

            equalsChecker(booking, result);
            verify(bookingRepository, times(1))
                .findByBookerIdAndStartBeforeAndEndAfter(any(), any(), any(), any());
            verify(bookingMapper, times(1)).toBookingResponseDto(booking);
            verify(userRepository, times(1)).findById(user2.getId());
        }

        @Test
        public void shouldGetPastByUser() {
            userRepositoryWhen(user2);
            when(
                bookingRepository.findByBookerIdAndEndBeforeAndStatusEquals(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> response = bookingService.findAllBookingByUserId(user2.getId(), PAST, pageable);

            assertEquals(1, response.size());

            BookingResponseDto result = response.get(0);

            equalsChecker(booking, result);
            verify(bookingRepository, times(1))
                .findByBookerIdAndEndBeforeAndStatusEquals(any(), any(), any(), any());
            verify(bookingMapper, times(1)).toBookingResponseDto(booking);
            verify(userRepository, times(1)).findById(user2.getId());
        }

        @Test
        public void shouldGetFutureByUser() {
            userRepositoryWhen(user2);
            when(
                bookingRepository.findByBookerIdAndStartAfter(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> response = bookingService.findAllBookingByUserId(user2.getId(), FUTURE, pageable);

            assertEquals(1, response.size());

            BookingResponseDto result = response.get(0);

            equalsChecker(booking, result);
            verify(bookingRepository, times(1))
                .findByBookerIdAndStartAfter(any(), any(), any());
            verify(bookingMapper, times(1)).toBookingResponseDto(booking);
            verify(userRepository, times(1)).findById(user2.getId());
        }

        @Test
        public void shouldGetWaitingByUser() {
            userRepositoryWhen(user2);
            when(
                bookingRepository.findByBookerIdAndStatusEquals(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> response =
                bookingService.findAllBookingByUserId(user2.getId(), BookingState.WAITING, pageable);

            assertEquals(1, response.size());

            BookingResponseDto result = response.get(0);

            equalsChecker(booking, result);
            verify(bookingRepository, times(1))
                .findByBookerIdAndStatusEquals(any(), any(), any());
            verify(bookingMapper, times(1)).toBookingResponseDto(booking);
            verify(userRepository, times(1)).findById(user2.getId());
        }

        @Test
        public void shouldGetRejectByUser() {
            userRepositoryWhen(user2);
            when(
                bookingRepository.findByBookerIdAndStatusEquals(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> response =
                bookingService.findAllBookingByUserId(user2.getId(), BookingState.REJECTED, pageable);

            assertEquals(1, response.size());

            BookingResponseDto result = response.get(0);

            equalsChecker(booking, result);
            verify(bookingRepository, times(1))
                .findByBookerIdAndStatusEquals(any(), any(), any());
            verify(bookingMapper, times(1)).toBookingResponseDto(booking);
            verify(userRepository, times(1)).findById(user2.getId());
        }

        @Test
        public void shouldGetAllEmptyIfNotUser() {
            userRepositoryWhen(user1);
            when(bookingRepository.findByBookerId(user1.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of()));

            List<BookingResponseDto> response = bookingService.findAllBookingByUserId(user1.getId(), ALL, pageable);

            assertTrue(response.isEmpty());

            verify(bookingRepository, times(1))
                .findByBookerId(user1.getId(), pageable);
            verify(userRepository, times(1)).findById(user1.getId());
        }

        @Test
        public void shouldGetCurrentEmptyIfNotUser() {
            userRepositoryWhen(user1);
            when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

            List<BookingResponseDto> response = bookingService.findAllBookingByUserId(user1.getId(), CURRENT, pageable);

            assertTrue(response.isEmpty());

            verify(bookingRepository, times(1))
                .findByBookerIdAndStartBeforeAndEndAfter(any(), any(), any(), any());
            verify(userRepository, times(1)).findById(user1.getId());
        }

        @Test
        public void shouldGetPastEmptyIfNotUser() {
            userRepositoryWhen(user1);
            when(
                bookingRepository.findByBookerIdAndEndBeforeAndStatusEquals(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

            List<BookingResponseDto> response = bookingService.findAllBookingByUserId(user1.getId(), PAST, pageable);

            assertTrue(response.isEmpty());

            verify(bookingRepository, times(1))
                .findByBookerIdAndEndBeforeAndStatusEquals(any(), any(), any(), any());
            verify(userRepository, times(1)).findById(user1.getId());
        }

        @Test
        public void shouldGetFutureEmptyIfNotUser() {
            userRepositoryWhen(user1);
            when(bookingRepository.findByBookerIdAndStartAfter(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

            List<BookingResponseDto> response = bookingService.findAllBookingByUserId(user1.getId(), FUTURE, pageable);

            assertTrue(response.isEmpty());

            verify(bookingRepository, times(1))
                .findByBookerIdAndStartAfter(any(), any(), any());
            verify(userRepository, times(1)).findById(user1.getId());
        }

        @Test
        public void shouldGetWaitingEmptyIfNotUser() {
            userRepositoryWhen(user1);
            when(bookingRepository.findByBookerIdAndStatusEquals(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

            List<BookingResponseDto> response =
                bookingService.findAllBookingByUserId(user1.getId(), BookingState.WAITING, pageable);

            assertTrue(response.isEmpty());

            verify(bookingRepository, times(1))
                .findByBookerIdAndStatusEquals(any(), any(), any());
            verify(userRepository, times(1)).findById(user1.getId());
        }

        @Test
        public void shouldGetRejectEmptyIfNotUser() {
            userRepositoryWhen(user1);
            when(bookingRepository.findByBookerIdAndStatusEquals(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

            List<BookingResponseDto> response =
                bookingService.findAllBookingByUserId(user1.getId(), BookingState.REJECTED, pageable);

            assertTrue(response.isEmpty());

            verify(bookingRepository, times(1))
                .findByBookerIdAndStatusEquals(any(), any(), any());
            verify(userRepository, times(1)).findById(user1.getId());
        }
    }

    @Nested
    class FindAllBookingByOwnerId {
        @Test
        public void shouldGetAllByOwner() {
            userRepositoryWhen(user1);
            when(itemRepository.findAllByOwnerId(user1.getId())).thenReturn(List.of(item1));
            when(bookingRepository.findByItemOwnerId(user1.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(booking)));

            when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> results = bookingService.findAllBookingByOwnerId(user1.getId(), ALL, pageable);

            assertEquals(1, results.size());
            BookingResponseDto response = results.get(0);

            equalsChecker(booking, response);
            verify(bookingRepository, times(1))
                .findByItemOwnerId(user1.getId(), pageable);
            verify(bookingMapper, times(1)).toBookingResponseDto(booking);
            verify(userRepository, times(1)).findById(user1.getId());
        }

        @Test
        public void shouldGetCurrentByOwner() {
            userRepositoryWhen(user1);
            when(itemRepository.findAllByOwnerId(user1.getId())).thenReturn(List.of(item1));
            when(bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(any(), any(), any(),
                any()))
                .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> response =
                bookingService.findAllBookingByOwnerId(user1.getId(), CURRENT, pageable);

            assertEquals(1, response.size());

            BookingResponseDto result = response.get(0);

            equalsChecker(booking, result);
            verify(bookingRepository, times(1))
                .findByItemOwnerIdAndStartBeforeAndEndAfter(any(), any(), any(), any());
            verify(bookingMapper, times(1)).toBookingResponseDto(booking);
            verify(userRepository, times(1)).findById(user1.getId());
        }

        @Test
        public void shouldGetPastByOwner() {
            userRepositoryWhen(user1);
            when(itemRepository.findAllByOwnerId(user1.getId())).thenReturn(List.of(item1));
            when(
                bookingRepository.findByItemOwnerIdAndEndBeforeAndStatusEquals(any(), any(), any(),
                    any()))
                .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> response = bookingService.findAllBookingByOwnerId(user1.getId(), PAST, pageable);

            assertEquals(1, response.size());

            BookingResponseDto result = response.get(0);

            equalsChecker(booking, result);
            verify(bookingRepository, times(1))
                .findByItemOwnerIdAndEndBeforeAndStatusEquals(any(), any(), any(), any());
            verify(bookingMapper, times(1)).toBookingResponseDto(booking);
            verify(userRepository, times(1)).findById(user1.getId());
        }

        @Test
        public void shouldGetFutureByOwner() {
            userRepositoryWhen(user1);
            when(itemRepository.findAllByOwnerId(user1.getId())).thenReturn(List.of(item1));
            when(
                bookingRepository.findByItemOwnerIdAndStartAfter(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> response = bookingService.findAllBookingByOwnerId(user1.getId(), FUTURE, pageable);

            assertEquals(1, response.size());

            BookingResponseDto result = response.get(0);

            equalsChecker(booking, result);
            verify(bookingRepository, times(1))
                .findByItemOwnerIdAndStartAfter(any(), any(), any());
            verify(bookingMapper, times(1)).toBookingResponseDto(booking);
            verify(userRepository, times(1)).findById(user1.getId());
        }

        @Test
        public void shouldGetWaitingByOwner() {
            userRepositoryWhen(user1);
            when(itemRepository.findAllByOwnerId(user1.getId())).thenReturn(List.of(item1));
            when(
                bookingRepository.findByItemOwnerIdAndStatusEquals(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> response =
                bookingService.findAllBookingByOwnerId(user1.getId(), BookingState.WAITING, pageable);

            assertEquals(1, response.size());

            BookingResponseDto result = response.get(0);

            equalsChecker(booking, result);
            verify(bookingRepository, times(1))
                .findByItemOwnerIdAndStatusEquals(any(), any(), any());
            verify(bookingMapper, times(1)).toBookingResponseDto(booking);
            verify(userRepository, times(1)).findById(user1.getId());
        }

        @Test
        public void shouldGetRejectByOwner() {
            userRepositoryWhen(user1);
            when(itemRepository.findAllByOwnerId(user1.getId())).thenReturn(List.of(item1));
            when(
                bookingRepository.findByItemOwnerIdAndStatusEquals(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(booking)));
            when(bookingMapper.toBookingResponseDto(booking)).thenReturn(bookingResponseDto);

            List<BookingResponseDto> response =
                bookingService.findAllBookingByOwnerId(user1.getId(), BookingState.REJECTED, pageable);

            assertEquals(1, response.size());

            BookingResponseDto result = response.get(0);

            equalsChecker(booking, result);
            verify(bookingRepository, times(1))
                .findByItemOwnerIdAndStatusEquals(any(), any(), any());
            verify(bookingMapper, times(1)).toBookingResponseDto(booking);
            verify(userRepository, times(1)).findById(user1.getId());
        }

        @Test
        public void shouldGetEmptyIfNotOwner() {
            userRepositoryWhen(user2);

            ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.findAllBookingByOwnerId(user2.getId(), ALL, pageable));
            assertEquals("У вас нет вещей. Сначала нужно добавить вещь.", exception.getMessage());


            verify(bookingRepository, never()).save(any());
        }
    }

}
