package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto addBooking(
        @RequestHeader("X-Sharer-User-Id") Long userId,
        @RequestBody @Valid BookingRequestDto bookingDto
    ) {
        log.info("POST [http://localhost:8080/bookings] : " +
            "Запрос на бронирование: {}, userId {}", bookingDto, userId);
        return bookingService.addBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBooking(
        @PathVariable Long bookingId,
        @RequestParam boolean approved,
        @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("PATCH [http://localhost:8080/bookings] : " +
            "Подтверждение или отклонение запроса на бронирование: " +
            "Бронирование c id: {}, пользователя с id: {}, approved: {}", bookingId, userId, approved);
        return bookingService.approveBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto findBookingById(
        @RequestHeader("X-Sharer-User-Id") Long userId,
        @PathVariable Long bookingId
    ) {
        log.info("GET [http://localhost:8080/bookings/{}] : " +
            "Запрос бронирования по id: {}, пользователем с id: {}", bookingId, bookingId, userId);
        return bookingService.findBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> findBookingByUserId(
        @RequestHeader("X-Sharer-User-Id") @NotNull Long userId,
        @RequestParam(required = false, defaultValue = "ALL") String state,
        @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
        @Positive @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("GET [http://localhost:8080/bookings/{}] : " +
            "Получение списка всех бронирований текущего пользователя id: {}, state: {}", userId, userId, state);
        return bookingService.findAllBookingByUserId(userId, BookingState.stateValid(state),
            PageRequest.of(from / size, size, Sort.by(Sort.Order.desc("start"))));
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> findBookingByOwnerId(
        @RequestHeader("X-Sharer-User-Id") @NotNull Long ownerId,
        @RequestParam(required = false, defaultValue = "ALL") String state,
        @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
        @Positive @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("GET [http://localhost:8080/bookings/owner] : " +
            "Получение списка бронирований для всех вещей текущего пользователя id: {}, state: {}", ownerId, state);
        return bookingService.findAllBookingByOwnerId(ownerId, BookingState.stateValid(state),
            PageRequest.of(from / size, size, Sort.by(Sort.Order.desc("start"))));
    }

}
