package ru.practicum.shareit.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, ItemMapper.class})
public interface BookingMapper {

    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "item", source = "item")
    @Mapping(target = "booker", source = "user")
    Booking toBooking(BookingRequestDto bookingDto, Item item, User user, BookingStatus status);

    BookingResponseDto toBookingResponseDto(Booking booking);

}
