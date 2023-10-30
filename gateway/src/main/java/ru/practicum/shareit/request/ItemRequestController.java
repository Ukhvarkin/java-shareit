package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.constants.Constants.X_SHARER_USER_ID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> addItemRequest(
        @RequestHeader(X_SHARER_USER_ID) @NotNull Long userId,
        @Valid @RequestBody ItemRequestDto itemRequestDto
    ) {
        log.info("POST [http://localhost:8080/requests] : " +
            "Запрос на запрос вещи: {}, userId {}", itemRequestDto, userId);
        return itemRequestClient.addItemRequest(itemRequestDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequestByRequestor(
        @RequestHeader(X_SHARER_USER_ID) @NotNull Long userId
    ) {
        log.info("GET [http://localhost:8080/requests] : " +
            "Запрос на получение запросов пользователя с id: {}", userId);
        return itemRequestClient.findAllItemRequestByRequestorId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequests(
        @RequestHeader(X_SHARER_USER_ID) @NotNull Long userId,
        @Valid @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
        @Valid @RequestParam(defaultValue = "10") @Positive Integer size
    ) {
        log.info("GET [http://localhost:8080/requests] : " +
            "Запрос на получение всех запросов пользователем с id: {}", userId);
        return itemRequestClient.findAllItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(
        @RequestHeader(X_SHARER_USER_ID) @NotNull Long userId,
        @PathVariable Long requestId
    ) {
        log.info("GET [http://localhost:8080/requests/{}] : " +
            "Запрос на получение запроса с id: {}, пользователем с id: {}", requestId, requestId, userId);
        return itemRequestClient.findItemRequestById(userId, requestId);
    }
}
