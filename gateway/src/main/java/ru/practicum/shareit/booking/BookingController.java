package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import ru.practicum.shareit.booking.enums.BookingState;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.constants.Constants.X_SHARER_USER_ID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
@Validated
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> bookItem(@RequestHeader(X_SHARER_USER_ID) @NotNull Long userId,
                                           @RequestBody @Valid BookingRequestDto bookingDto
    ) {
        log.info("Запрос на бронирование: {}, userId {}", bookingDto, userId);
        return bookingClient.bookItem(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> update(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                         @PathVariable Long bookingId,
                                         @RequestParam Boolean approved) {
        log.info("Подтверждение или отклонение запроса на бронирование: " +
            "Бронирование c id: {}, пользователя с id: {}, approved: {}", bookingId, userId, approved);
        return bookingClient.update(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(X_SHARER_USER_ID) @NotNull Long userId,
                                             @PathVariable Long bookingId
    ) {
        log.info("Запрос бронирования по id: {}, пользователем с id: {}", bookingId, userId);
        return bookingClient.getBooking(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByUser(@RequestHeader(X_SHARER_USER_ID) @NotNull Long userId,
                                                    @RequestParam(required = false, defaultValue = "ALL") String state,
                                                    @Valid @RequestParam(defaultValue = "0")
                                                    @PositiveOrZero Integer from,
                                                    @Valid @RequestParam(defaultValue = "10") @Positive Integer size
    ) {
        log.info("Получение списка всех бронирований текущего пользователя id: {}, state: {}", userId, state);
        return bookingClient.getBookingByUser(userId, BookingState.stateValid(state), from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwner(@RequestHeader(X_SHARER_USER_ID) @NotNull Long ownerId,
                                                     @RequestParam(required = false, defaultValue = "ALL") String state,
                                                     @Valid @RequestParam(defaultValue = "0")
                                                     @PositiveOrZero Integer from,
                                                     @Valid @RequestParam(defaultValue = "10") @Positive Integer size
    ) {
        log.info("Получение списка бронирований для всех вещей текущего пользователя id: {}, state: {}", ownerId,
            state);
        return bookingClient.getBookingByOwner(ownerId, BookingState.stateValid(state), from, size);
    }

}
