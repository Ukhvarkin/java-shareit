package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponseDto addBooking(BookingRequestDto bookingDto, Long bookerId) {
        if (bookingDto.getItemId() == null) {
            throw new ValidationException("Не указан идентификатор вещи (itemId).");
        }
        validBookingTime(bookingDto);
        Item item = itemRepository.findById(bookingDto.getItemId())
            .orElseThrow(() -> new ItemNotFoundException("Не найдена вещь с id: " + bookingDto.getItemId()));
        log.info("Найдена вещь с id: {}", item.getId());
        if (!item.getAvailable()) {
            throw new ValidationException("Бронь недоступна.");
        }
        User booker = userRepository.findById(bookerId)
            .orElseThrow(
                () -> new UserNotFoundException("Не найден пользователь с id: " + bookerId + " не существует."));
        if (bookerId.equals(item.getOwner().getId())) {
            throw new ItemNotFoundException("Вы не можете забронировать свою вещь");
        }
        log.info("Найден пользователь с id: {}", booker.getId());

        Long itemId = item.getId();
        LocalDateTime bookingStart = bookingDto.getStart();
        LocalDateTime bookingEnd = bookingDto.getEnd();

        if (hasApprovedBookings(itemId, bookingStart, bookingEnd) ||
            hasOverlappingBookings(itemId, bookingStart, bookingEnd)) {
            throw new BookingNotFoundException("В данный момент бронирование не доступно.");
        }

        Booking booking = bookingMapper.toBooking(bookingDto, item, booker, BookingStatus.WAITING);
        return bookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    private boolean hasApprovedBookings(Long itemId, LocalDateTime start, LocalDateTime end) {
        return !bookingRepository.findBookingsByItemIdAndTimeRangeAndStatus(
            itemId, start, end, BookingStatus.APPROVED).isEmpty()
            || !bookingRepository.findByItemIdAndTimeRangeAndStatus(
            itemId, start, end, BookingStatus.APPROVED).isEmpty();
    }

    private boolean hasOverlappingBookings(Long itemId, LocalDateTime start, LocalDateTime end) {
        return bookingRepository.hasOverlappingBookings1(itemId, start, end);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long bookingId, Long userId, boolean status) {
        userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Не найден пользователь с id: " + userId + " не существует."));

        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingNotFoundException("Нет бронирования с id: " + bookingId));

        if (!userId.equals(booking.getItem().getOwner().getId())) {
            throw new UserNotFoundException("Только владелец может подтверждать или отклонять бронирование.");
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException("Бронирование уже подтверждено.");
        }
        booking.setStatus(status ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        log.info("Владелец установил статус бронирования: {}", booking.getStatus());
        return bookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto findBookingById(Long bookingId, Long userId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Не найден пользователь с id: " + userId + " не существует."));

        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingNotFoundException("Не существует бронирования с id: " + bookingId));

        if (!userId.equals(booking.getBooker().getId()) && !userId.equals(booking.getItem().getOwner().getId())) {
            throw new UserNotFoundException("Просмотр бронирования доступно только автору или владельцу.");
        }
        return bookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> findAllBookingByUserId(Long userId, BookingState state, Pageable pageable) {
        userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Не найден пользователь с id: " + userId));
        List<Booking> result = null;
        LocalDateTime time = LocalDateTime.now();

        switch (state) {
            case ALL:
                result = bookingRepository.findByBookerId(userId, pageable).toList();
                break;
            case CURRENT:
                result = bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(
                    userId, time, time, pageable).toList();
                break;
            case PAST:
                result = bookingRepository.findByBookerIdAndEndBeforeAndStatusEquals(
                    userId, time, BookingStatus.APPROVED, pageable).toList();
                break;
            case FUTURE:
                result = bookingRepository.findByBookerIdAndStartAfter(
                    userId, time, pageable).toList();
                break;
            case WAITING:
                result = bookingRepository.findByBookerIdAndStatusEquals(
                    userId, BookingStatus.WAITING, pageable).toList();
                break;
            case REJECTED:
                result = bookingRepository.findByBookerIdAndStatusEquals(
                    userId, BookingStatus.REJECTED, pageable).toList();
                break;
        }

        return result.stream()
            .map(bookingMapper::toBookingResponseDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> findAllBookingByOwnerId(Long ownerId, BookingState state, Pageable pageable) {
        userRepository.findById(ownerId)
            .orElseThrow(() -> new UserNotFoundException("Не найден пользователь с id: " + ownerId));

        if (itemRepository.findAllByOwnerId(ownerId).isEmpty()) {
            throw new ValidationException("У вас нет вещей. Сначала нужно добавить вещь.");
        }
        LocalDateTime time = LocalDateTime.now();
        List<Booking> result = null;

        switch (state) {
            case ALL:
                result = bookingRepository.findByItemOwnerId(ownerId, pageable).toList();
                break;
            case CURRENT:
                result = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(
                    ownerId, time, time, pageable).toList();
                break;
            case PAST:
                result = bookingRepository.findByItemOwnerIdAndEndBeforeAndStatusEquals(
                    ownerId, time, BookingStatus.APPROVED, pageable).toList();
                break;
            case FUTURE:
                result = bookingRepository.findByItemOwnerIdAndStartAfter(
                    ownerId, time, pageable).toList();
                break;
            case WAITING:
                result = bookingRepository.findByItemOwnerIdAndStatusEquals(
                    ownerId, BookingStatus.WAITING, pageable).toList();
                break;
            case REJECTED:
                result = bookingRepository.findByItemOwnerIdAndStatusEquals(
                    ownerId, BookingStatus.REJECTED, pageable).toList();
                break;
        }
        return result.stream()
            .map(bookingMapper::toBookingResponseDto)
            .collect(Collectors.toList());
    }

    private void validBookingTime(BookingRequestDto bookingDto) {
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            String message = "Введите время.";
            log.warn(message);
            throw new ValidationException(message);
        }
        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            String message = "Время старта не может быть в прошлом.";
            log.warn(message);
            throw new ValidationException(message);
        }
        if (bookingDto.getEnd().isBefore(LocalDateTime.now())) {
            String message = "Время окончания не может быть в прошлом.";
            log.warn(message);
            throw new ValidationException(message);
        }
        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            String message = "Время окончания не может быть раньше начала.";
            log.warn(message);
            throw new ValidationException(message);
        }
        if (bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            String message = "Время старта не может быть равно окончанию.";
            log.warn(message);
            throw new ValidationException(message);
        }
    }
}
