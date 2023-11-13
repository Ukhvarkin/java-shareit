package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Getter
@Setter
@Builder
@ToString
public class BookingItemDto {
    Long id;
    Long bookerId;
    LocalDateTime start;
    LocalDateTime end;
}
