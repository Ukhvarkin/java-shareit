package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDto> addBooking(
        @RequestHeader("X-Sharer-User-Id") @NotNull Long userId,
        @RequestBody @Valid BookingRequestDto bookingDto
    ) {
        log.info("POST [http://localhost:8080/bookings] : " +
            "Запрос на бронирование: {}, userId {}", bookingDto, userId);
        return ResponseEntity.ok(bookingService.addBooking(bookingDto, userId));
    }

    @PatchMapping("{bookingId}")
    public ResponseEntity<BookingResponseDto> approveBooking(
        @PathVariable Long bookingId,
        @RequestParam boolean approved,
        @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("PATCH [http://localhost:8080/bookings] : " +
            "Подтверждение или отклонение запроса на бронирование: " +
            "Бронирование c id: {}, пользователя с id: {}, approved: {}", bookingId, userId, approved);
        return ResponseEntity.ok(bookingService.approveBooking(bookingId, userId, approved));
    }

    @GetMapping("{bookingId}")
    public ResponseEntity<BookingResponseDto> findBookingById(
        @RequestHeader("X-Sharer-User-Id") @NotNull Long userId,
        @PathVariable Long bookingId
    ) {
        log.info("GET [http://localhost:8080/bookings/{bookingId}] : " +
            "Запрос бронирования по id: {}, пользователем с id: {}", bookingId, userId);
        return ResponseEntity.ok(bookingService.findBookingById(bookingId, userId));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> findBookingByUserId(
        @RequestHeader("X-Sharer-User-Id") @NotNull Long userId,
        @RequestParam(required = false, defaultValue = "ALL") String state
    ) {
        log.info("GET [http://localhost:8080/bookings] : " +
            "Получение списка всех бронирований текущего пользователя id: {}, state: {}", userId, state);
        return ResponseEntity.ok(bookingService.findAllBookingByUserId(userId, BookingState.stateValid(state)));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponseDto>> findBookingByOwnerId(
        @RequestHeader("X-Sharer-User-Id") @NotNull Long ownerId,
        @RequestParam(required = false, defaultValue = "ALL") String state
    ) {
        log.info("GET [http://localhost:8080/bookings/owner] : " +
            "Получение списка бронирований для всех вещей текущего пользователя id: {}, state: {}", ownerId, state);
        return ResponseEntity.ok(bookingService.findAllBookingByOwnerId(ownerId, BookingState.stateValid(state)));
    }

}