package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.booking.BookingStatus.APPROVED;

@ExtendWith(MockitoExtension.class)
class BookingMapperTest {
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private BookingMapperImpl bookingMapper;

    @Nested
    @DisplayName("Маппинг в Booking")
    class ToBooking {
        @Test
        void shouldReturnBooking() {
            User user = User.builder()
                .id(1L)
                .name("One")
                .email("one@yandex.ru")
                .build();

            Item item = Item.builder()
                .id(1L)
                .name("item")
                .description("description")
                .available(true)
                .owner(user)
                .requestId(1L)
                .build();

            BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                .start(LocalDateTime.now().plusMinutes(5))
                .end(LocalDateTime.now().plusMinutes(10))
                .itemId(1L)
                .build();

            BookingStatus status = BookingStatus.APPROVED;

            Booking booking = bookingMapper.toBooking(bookingRequestDto, item, user, status);

            assertEquals(status, booking.getStatus());
        }

        @Test
        void shouldReturnNull() {
            Booking booking = bookingMapper.toBooking(null, null, null, null);

            assertNull(booking);
        }
    }

    @Nested
    @DisplayName("Маппинг в BookingResponseDto")
    class ToBookingResponseDtoTest {
        @Test
        void shouldReturnBookingResponseDto() {
            User user = User.builder()
                .id(1L)
                .name("One")
                .email("one@yandex.ru")
                .build();

            Item item = Item.builder()
                .id(1L)
                .name("item")
                .description("description")
                .available(true)
                .owner(user)
                .requestId(1L)
                .build();

            Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().minusYears(5))
                .item(item)
                .booker(user)
                .status(APPROVED)
                .build();

            when(userMapper.toUserDto(booking.getBooker())).thenReturn(any());

            BookingResponseDto bookingResponseDto = bookingMapper.toBookingResponseDto(booking);

            assertEquals(bookingResponseDto.getStatus(), booking.getStatus());
        }

        @Test
        void shouldReturnNull() {
            BookingResponseDto bookingResponseDto = bookingMapper.toBookingResponseDto(null);

            assertNull(bookingResponseDto);
        }
    }
}