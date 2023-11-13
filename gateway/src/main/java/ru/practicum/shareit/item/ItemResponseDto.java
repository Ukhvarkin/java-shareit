package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.booking.dto.BookingItemDto;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponseDto {
    Long id;
    String name;
    String description;
    Boolean available;
    Long ownerId;
    Long requestId;
    BookingItemDto lastBooking;
    BookingItemDto nextBooking;
    List<CommentDto> comments;
}
