package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingControllerTest {
    @MockBean
    private BookingService bookingService;
    private BookingRequestDto bookingRequestDto;
    private BookingResponseDto bookingResponseDto;

    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    private final int from = 0;
    private final int size = 10;
    private final Pageable pageable =
        PageRequest.of(from / size, size, Sort.by(Sort.Order.desc("start")));

    private final User user1 = User.builder()
        .id(1L)
        .name("One")
        .email("one@yandex.ru")
        .build();
    private final User user2 = User.builder()
        .id(2L)
        .name("Two")
        .email("two@yandex.ru")
        .build();
    private final UserDto userDto2 = UserDto.builder()
        .id(user2.getId())
        .name(user2.getName())
        .email(user2.getEmail())
        .build();
    private final ItemResponseDto itemDto = ItemResponseDto.builder()
        .id(1L)
        .name("item")
        .description("description")
        .available(true)
        .ownerId(user1.getId())
        .requestId(1L)
        .build();
    private final BookingResponseDto bookingResponseDto1 = BookingResponseDto.builder()
        .id(1L)
        .start(LocalDateTime.now().plusMinutes(1))
        .end(LocalDateTime.now().plusMinutes(5))
        .item(itemDto)
        .booker(userDto2)
        .status(BookingStatus.WAITING)
        .build();

    private final BookingResponseDto bookingResponseDto2 = BookingResponseDto.builder()
        .id(2L)
        .start(LocalDateTime.now().plusMinutes(10))
        .end(LocalDateTime.now().plusMinutes(15))
        .item(itemDto)
        .booker(userDto2)
        .status(BookingStatus.WAITING)
        .build();

    @BeforeEach
    public void beforeEach() {
        bookingRequestDto = BookingRequestDto.builder()
            .start(LocalDateTime.now().plusMinutes(1))
            .end(LocalDateTime.now().plusMinutes(5))
            .itemId(1L)
            .build();
        bookingResponseDto = BookingResponseDto.builder()
            .id(1L)
            .start(LocalDateTime.now().plusMinutes(1))
            .end(LocalDateTime.now().plusMinutes(5))
            .item(itemDto)
            .booker(userDto2)
            .status(BookingStatus.WAITING)
            .build();
    }

    @Nested
    @DisplayName("POST")
    public class MethodPost {
        @Test
        @DisplayName("Добавление нового бронирования")
        public void methodPost_NewBookingValidTest() throws Exception {
            when(bookingService.addBooking(ArgumentMatchers.any(BookingRequestDto.class),
                ArgumentMatchers.eq(user2.getId())))
                .thenReturn(bookingResponseDto1);

            mockMvc.perform(post("/bookings")
                    .header("X-Sharer-User-Id", user2.getId())
                    .content(objectMapper.writeValueAsString(bookingRequestDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookingResponseDto1)));

        }

        @Test
        @DisplayName("Добавление нового бронирования : item id: null")
        public void methodPost_NewBooking_ItemIdNullTest() throws Exception {
            bookingRequestDto.setItemId(null);

            mockMvc.perform(post("/bookings")
                    .header("X-Sharer-User-Id", user2.getId())
                    .content(objectMapper.writeValueAsString(bookingRequestDto))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

            verify(bookingService, never()).addBooking(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Добавление нового бронирования : время начала в прошлом")
        public void methodPost_NewBooking_StartInPastTest() throws Exception {
            bookingRequestDto.setStart(LocalDateTime.now().minusMinutes(5));
            bookingRequestDto.setEnd(LocalDateTime.now().plusMinutes(5));

            mockMvc.perform(post("/bookings")
                    .header("X-Sharer-User-Id", user2.getId())
                    .content(objectMapper.writeValueAsString(bookingRequestDto))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

            verify(bookingService, never()).addBooking(ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Добавление нового бронирования : время раньше времени старта")
        public void methodPost_NewBooking_EndInPresentTest() throws Exception {
            bookingRequestDto.setStart(LocalDateTime.now().plusMinutes(5));
            bookingRequestDto.setEnd(LocalDateTime.now());

            mockMvc.perform(post("/bookings")
                    .header("X-Sharer-User-Id", user2.getId())
                    .content(objectMapper.writeValueAsString(bookingRequestDto))
                    .characterEncoding(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

            verify(bookingService, never()).addBooking(ArgumentMatchers.any(), ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("PATCH")
    public class MethodPatch {
        @Test
        @DisplayName("Подтверждение бронирования")
        public void methodPatch_ApprovedTest() throws Exception {
            bookingResponseDto.setStatus(BookingStatus.APPROVED);

            when(bookingService.approveBooking(
                ArgumentMatchers.eq(bookingResponseDto.getId()),
                ArgumentMatchers.eq(user2.getId()),
                ArgumentMatchers.eq(true)))
                .thenReturn(bookingResponseDto);

            mockMvc.perform(patch("/bookings/{id}?approved={approved}", bookingResponseDto.getId(), true)
                    .header("X-Sharer-User-Id", user2.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookingResponseDto)));

            verify(bookingService, times(1)).approveBooking(
                ArgumentMatchers.eq(bookingResponseDto.getId()), ArgumentMatchers.eq(user2.getId()),
                ArgumentMatchers.eq(true));
        }

        @Test
        @DisplayName("Отказ бронирования")
        public void methodPatch_RejectTest() throws Exception {
            bookingResponseDto.setStatus(BookingStatus.REJECTED);

            when(bookingService.approveBooking(
                ArgumentMatchers.eq(bookingResponseDto.getId()),
                ArgumentMatchers.eq(user2.getId()),
                ArgumentMatchers.eq(false)))
                .thenReturn(bookingResponseDto);

            mockMvc.perform(patch("/bookings/{id}?approved={approved}", bookingResponseDto.getId(), false)
                    .header("X-Sharer-User-Id", user2.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookingResponseDto)));

            verify(bookingService, times(1)).approveBooking(
                ArgumentMatchers.eq(bookingResponseDto.getId()),
                ArgumentMatchers.eq(user2.getId()),
                ArgumentMatchers.eq(false));
        }
    }

    @Nested
    @DisplayName("GET")
    public class MethodGet {
        @Test
        @DisplayName("Получение бронирования по id")
        public void methodGet_BookingByIdTest() throws Exception {
            when(bookingService.findBookingById(
                ArgumentMatchers.eq(bookingResponseDto1.getId()),
                ArgumentMatchers.eq(user2.getId())))
                .thenReturn(bookingResponseDto1);

            mockMvc.perform(get("/bookings/{id}", bookingResponseDto1.getId())
                    .header("X-Sharer-User-Id", user2.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookingResponseDto1)));

            verify(bookingService, times(1))
                .findBookingById(
                    ArgumentMatchers.eq(bookingResponseDto1.getId()),
                    ArgumentMatchers.eq(user2.getId()));
        }

        @Test
        @DisplayName("Получение бронирования пользователем, state: default, from: 0, size: 10")
        public void methodGet_WithDefaultStateByUserTest() throws Exception {

            when(bookingService.findAllBookingByUserId(
                ArgumentMatchers.eq(userDto2.getId()),
                ArgumentMatchers.eq(BookingState.ALL),
                ArgumentMatchers.eq(pageable)))
                .thenReturn(List.of(bookingResponseDto1, bookingResponseDto2));

            mockMvc.perform(get("/bookings?from={from}&size={size}", from, size)
                    .header("X-Sharer-User-Id", user2.getId()))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(objectMapper.writeValueAsString(List.of(bookingResponseDto1, bookingResponseDto2))));

            verify(bookingService, times(1))
                .findAllBookingByUserId(
                    ArgumentMatchers.eq(userDto2.getId()),
                    ArgumentMatchers.eq(BookingState.ALL),
                    ArgumentMatchers.eq(pageable));
        }

        @Test
        @DisplayName("Получение бронирования пользователем, state: ALL, from: 0, size: 10")
        public void methodGet_WithValidStateByUserTest() throws Exception {
            when(bookingService.findAllBookingByUserId(
                ArgumentMatchers.eq(userDto2.getId()),
                ArgumentMatchers.eq(BookingState.ALL),
                ArgumentMatchers.eq(pageable)))
                .thenReturn(List.of(bookingResponseDto1, bookingResponseDto2));

            mockMvc.perform(get("/bookings?state={state}&from={from}&size={size}", "ALL", from, size)
                    .header("X-Sharer-User-Id", user2.getId()))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(objectMapper.writeValueAsString(List.of(bookingResponseDto1, bookingResponseDto2))));

            verify(bookingService, times(1))
                .findAllBookingByUserId(
                    ArgumentMatchers.eq(userDto2.getId()),
                    ArgumentMatchers.eq(BookingState.ALL),
                    ArgumentMatchers.eq(pageable));
        }

        @Test
        @DisplayName("Получение бронирования пользователем, state: Unknown, from: 0, size: 10")
        public void methodGet_ThrowExceptionIfStateUnknownByUserTest() throws Exception {
            mockMvc.perform(get("/bookings?state={state}&from={from}&size={size}", "Unknown", from, size)
                    .header("X-Sharer-User-Id", user2.getId()))
                .andExpect(status().is4xxClientError());

            verify(bookingService, never())
                .findAllBookingByOwnerId(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Получение бронирования владельцем, state: default, from: 0, size: 10")
        public void methodGet_WithValidStateByOwnerTest() throws Exception {
            when(bookingService.findAllBookingByOwnerId(
                ArgumentMatchers.eq(itemDto.getOwnerId()),
                ArgumentMatchers.eq(BookingState.ALL),
                ArgumentMatchers.eq(pageable)))
                .thenReturn(List.of(bookingResponseDto1, bookingResponseDto2));

            mockMvc.perform(get("/bookings/owner?state={state}&from={from}&size={size}", "ALL", from, size)
                    .header("X-Sharer-User-Id", user1.getId()))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(objectMapper.writeValueAsString(List.of(bookingResponseDto1, bookingResponseDto2))));

            verify(bookingService, times(1))
                .findAllBookingByOwnerId(ArgumentMatchers.eq(
                        itemDto.getOwnerId()),
                    ArgumentMatchers.eq(BookingState.ALL),
                    ArgumentMatchers.eq(pageable));
        }

        @Test
        @DisplayName("Получение бронирования владельцем, state: ALL, from: 0, size: 10")
        public void methodGet_WithDefaultStateByOwnerTest() throws Exception {
            when(bookingService.findAllBookingByOwnerId(
                ArgumentMatchers.eq(itemDto.getOwnerId()),
                ArgumentMatchers.eq(BookingState.ALL),
                ArgumentMatchers.eq(pageable)))
                .thenReturn(List.of(bookingResponseDto1, bookingResponseDto2));

            mockMvc.perform(get("/bookings/owner?from={from}&size={size}", from, size)
                    .header("X-Sharer-User-Id", user1.getId()))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(objectMapper.writeValueAsString(List.of(bookingResponseDto1, bookingResponseDto2))));

            verify(bookingService, times(1))
                .findAllBookingByOwnerId(ArgumentMatchers.eq(
                        itemDto.getOwnerId()),
                    ArgumentMatchers.eq(BookingState.ALL),
                    ArgumentMatchers.eq(pageable));
        }

        @Test
        @DisplayName("Получение бронирования владельцем, state: Unknown, from: 0, size: 10")
        public void methodGet_ThrowExceptionIfStateUnknownTest() throws Exception {
            mockMvc.perform(get("/bookings/owner?state={state}&from={from}&size={size}", "Unknown", from, size)
                    .header("X-Sharer-User-Id", user1.getId()))
                .andExpect(status().is4xxClientError());

            verify(bookingService, never())
                .findAllBookingByOwnerId(
                    ArgumentMatchers.any(),
                    ArgumentMatchers.any(),
                    ArgumentMatchers.any());
        }
    }
}