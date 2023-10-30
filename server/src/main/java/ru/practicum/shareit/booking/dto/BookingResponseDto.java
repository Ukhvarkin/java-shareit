package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class BookingResponseDto {
    Long id;
    LocalDateTime start;
    LocalDateTime end;
    ItemResponseDto item;
    UserDto booker;
    BookingStatus status;
}

