package ru.practicum.shareit.item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.user.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(target = "id", source = "itemDto.id")
    @Mapping(target = "name", source = "itemDto.name")
    Item toItem(ItemResponseDto itemDto, User owner, List<Comment> comments);

    @Mapping(target = "ownerId", expression = "java(item.getOwner().getId())")
    ItemDto toItemDto(Item item);

    @Mapping(target = "id", expression = "java(item.getId())")
    @Mapping(target = "ownerId", expression = "java(item.getOwner().getId())")
    @Mapping(target = "comments", source = "comments")
    ItemResponseDto toItemResponseDto(Item item, BookingItemDto lastBooking, BookingItemDto nextBooking,
                                      List<CommentDto> comments);

    @Mapping(target = "bookerId", expression = "java(booking.getBooker().getId())")
    BookingItemDto toBookingItemDto(Booking booking);
}
