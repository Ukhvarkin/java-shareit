package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Slf4j
@Service
public class BookingClient extends BaseClient {

    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
            builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build()
        );
    }

    public ResponseEntity<Object> bookItem(BookingRequestDto bookingDto, Long userId) {
        return post("", userId, null, bookingDto);
    }

    public ResponseEntity<Object> update(Long bookingId, long userId, boolean approved) {
        log.info("approved={}", approved);
        return patch("/" + bookingId + "?approved=" + approved, userId);
    }

    public ResponseEntity<Object> getBooking(Long bookingId, Long userId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getBookingByUser(Long userId, BookingState bookingState, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
            "state", bookingState.name(),
            "from", from,
            "size", size
        );
        return get("?state={state}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getBookingByOwner(Long ownerId, BookingState bookingState, Integer from,
                                                    Integer size) {
        Map<String, Object> parameters = Map.of(
            "state", bookingState.name(),
            "from", from,
            "size", size
        );
        return get("/owner?state={state}&from={from}&size={size}", ownerId, parameters);
    }
}
