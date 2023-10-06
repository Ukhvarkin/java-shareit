package ru.practicum.shareit.item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.item.comment.CommentDto;

import java.util.List;

@Mapper
public interface ItemMapper {

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "request", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Item toItem(ItemDto itemDto);

    default ItemDto toItemDto(Item item, BookingItemDto lastBooking, BookingItemDto nextBooking,
                              List<CommentDto> comments) {
        ItemDto.BookingForItemDto lastBookingToAdd = null;
        ItemDto.BookingForItemDto nextBookingToAdd = null;

        if (lastBooking != null) {
            lastBookingToAdd = new ItemDto.BookingForItemDto(
                lastBooking.getId(),
                lastBooking.getStart(),
                lastBooking.getEnd(),
                lastBooking.getBookerId()
            );
        }

        if (nextBooking != null) {
            nextBookingToAdd = new ItemDto.BookingForItemDto(
                nextBooking.getId(),
                nextBooking.getStart(),
                nextBooking.getEnd(),
                nextBooking.getBookerId()
            );
        }

        return new ItemDto(
            item.getId(),
            item.getName(),
            item.getDescription(),
            item.getAvailable(),
            lastBookingToAdd,
            nextBookingToAdd,
            comments
        );
    }

    @Mapping(target = "bookerId", expression = "java(booking.getBooker().getId())")
    BookingItemDto bookingToBookingItemDto(Booking booking);
}
