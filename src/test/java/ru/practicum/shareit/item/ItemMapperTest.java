package ru.practicum.shareit.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static ru.practicum.shareit.booking.BookingStatus.APPROVED;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ItemMapperTest {
    @InjectMocks
    private ItemMapperImpl itemMapper;

    private final LocalDateTime dateTime = LocalDateTime.of(2000, 1, 1, 1, 1, 1);
    private final User user = User.builder()
        .id(1L)
        .name("One")
        .email("one@yandex.ru")
        .build();
    private final CommentDto commentRequestDto = CommentDto.builder()
        .text("commentRequestDto text")
        .build();
    private final Item item = Item.builder()
        .id(1L)
        .name("item")
        .description("description")
        .available(true)
        .owner(user)
        .requestId(1L)
        .build();

    private final ItemResponseDto itemResponseDto = ItemResponseDto.builder()
        .id(1L)
        .name("item")
        .description("description")
        .available(true)
        .build();

    private final Comment comment1 = Comment.builder()
        .id(1L)
        .text("text1")
        .created(dateTime)
        .author(user)
        .item(item)
        .build();
    private final Comment comment2 = Comment.builder()
        .id(2L)
        .text("text2")
        .created(dateTime)
        .author(user)
        .item(item)
        .build();

    private final CommentDto commentDto1 = CommentDto.builder()
        .id(comment1.getId())
        .text(comment1.getText())
        .created(comment1.getCreated())
        .authorName(comment1.getAuthor().getName())
        .build();

    private final CommentDto commentDto2 = CommentDto.builder()
        .id(comment2.getId())
        .text(comment2.getText())
        .created(comment2.getCreated())
        .authorName(comment2.getAuthor().getName())
        .build();
    private final Booking booking = Booking.builder()
        .id(1L)
        .start(dateTime.minusYears(10))
        .end(dateTime.minusYears(9))
        .item(item)
        .booker(user)
        .status(APPROVED)
        .build();
    private final ItemDto itemDto = ItemDto.builder()
        .id(1L)
        .name("item name")
        .description("item description")
        .available(true)
        .ownerId(user.getId())
        .requestId(1L)
        .build();
    private final BookingItemDto lastBooking = BookingItemDto.builder()
        .id(1L)
        .bookerId(user.getId())
        .start(dateTime)
        .end(dateTime.plusHours(1))
        .build();
    private final BookingItemDto nextBooking = BookingItemDto.builder()
        .id(2L)
        .bookerId(user.getId())
        .start(dateTime.plusHours(2))
        .end(dateTime.plusHours(3))
        .build();

    @Nested
    @DisplayName("Маппинг в Item")
    class ToItem {
        @Test
        public void shouldReturnItemDto() {
            Item result = itemMapper.toItem(itemResponseDto, user, List.of(comment1, comment2));

            assertEquals(itemResponseDto.getId(), result.getId());
            assertEquals(itemResponseDto.getName(), result.getName());
            assertEquals(itemResponseDto.getDescription(), result.getDescription());
            assertEquals(itemResponseDto.getAvailable(), result.getAvailable());
            assertEquals(user.getName(), result.getOwner().getName());
            assertEquals(user.getEmail(), result.getOwner().getEmail());
            assertEquals(itemResponseDto.getRequestId(), result.getRequestId());

        }

        @Test
        public void shouldReturnNull() {
            Item result = itemMapper.toItem(null, null, null);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Маппинг в ItemDto")
    class ToItemDto {
        @Test
        public void shouldReturnItemDto() {
            ItemDto result = itemMapper.toItemDto(item);

            assertEquals(item.getId(), result.getId());
            assertEquals(item.getName(), result.getName());
            assertEquals(item.getDescription(), result.getDescription());
            assertEquals(item.getAvailable(), result.getAvailable());
            assertEquals(item.getOwner().getId(), result.getOwnerId());
            assertEquals(item.getRequestId(), result.getRequestId());
        }

        @Test
        public void shouldReturnNull() {
            ItemDto result = itemMapper.toItemDto(null);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Маппинг в ItemResponseDto")
    class ToItemResponseDto {
        @Test
        public void shouldReturnItemResponseDto() {
            ItemResponseDto result =
                itemMapper.toItemResponseDto(item, lastBooking, nextBooking, List.of(commentDto1, commentDto2));

            assertEquals(item.getId(), result.getId());
            assertEquals(item.getName(), result.getName());
            assertEquals(item.getDescription(), result.getDescription());

            assertEquals(lastBooking.getId(), result.getLastBooking().getId());
            assertEquals(lastBooking.getBookerId(), result.getLastBooking().getBookerId());
            assertEquals(lastBooking.getStart(), result.getLastBooking().getStart());
            assertEquals(lastBooking.getEnd(), result.getLastBooking().getEnd());

            assertEquals(nextBooking.getId(), result.getNextBooking().getId());
            assertEquals(nextBooking.getBookerId(), result.getNextBooking().getBookerId());
            assertEquals(nextBooking.getStart(), result.getNextBooking().getStart());
            assertEquals(nextBooking.getEnd(), result.getNextBooking().getEnd());

            CommentDto commentFromResult1 = result.getComments().get(0);
            CommentDto commentFromResult2 = result.getComments().get(1);

            assertEquals(comment1.getId(), commentFromResult1.getId());
            assertEquals(comment1.getText(), commentFromResult1.getText());
            assertEquals(comment1.getCreated(), commentFromResult1.getCreated());
            assertEquals(comment1.getAuthor().getName(), commentFromResult1.getAuthorName());

            assertEquals(comment2.getId(), commentFromResult2.getId());
            assertEquals(comment2.getText(), commentFromResult2.getText());
            assertEquals(comment2.getCreated(), commentFromResult2.getCreated());
            assertEquals(comment2.getAuthor().getName(), commentFromResult2.getAuthorName());
        }

        @Test
        public void shouldReturnNull() {
            ItemResponseDto result = itemMapper.toItemResponseDto(null, null, null, null);
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Маппинг в BookingItemDto")
    class ToBookingItemDto {
        @Test
        public void shouldReturnBookingItemDto() {
            BookingItemDto result = itemMapper.toBookingItemDto(booking);

            assertEquals(booking.getId(), result.getId());
            assertEquals(booking.getBooker().getId(), result.getBookerId());
            assertEquals(booking.getStart(), result.getStart());
            assertEquals(booking.getEnd(), result.getEnd());
        }

        @Test
        public void shouldReturnNull() {
            BookingItemDto result = itemMapper.toBookingItemDto(null);

            assertNull(result);
        }
    }
}