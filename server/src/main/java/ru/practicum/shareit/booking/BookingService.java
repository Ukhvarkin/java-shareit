package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto addBooking(BookingRequestDto bookingDto, Long userId);

    BookingResponseDto approveBooking(Long bookingId, Long userId, boolean approved);

    BookingResponseDto findBookingById(Long bookingId, Long userId);

    List<BookingResponseDto> findAllBookingByUserId(Long userId, BookingState state, Pageable pageable);

    List<BookingResponseDto> findAllBookingByOwnerId(Long ownerId, BookingState state, Pageable pageable);
}
