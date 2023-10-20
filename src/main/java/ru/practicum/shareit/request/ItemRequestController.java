package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ResponseEntity<ItemRequestDto> addItemRequest(
        @RequestHeader("X-Sharer-User-Id") @NotNull Long userId,
        @Valid @RequestBody ItemRequestDto itemRequestDto
    ) {
        log.info("POST [http://localhost:8080/requests] : " +
            "Запрос на запрос вещи: {}, userId {}", itemRequestDto, userId);
        return ResponseEntity.ok(itemRequestService.addItemRequest(itemRequestDto, userId));
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestResponseDto>> getItemRequestByRequestor(
        @RequestHeader("X-Sharer-User-Id") @NotNull Long userId
    ) {
        log.info("GET [http://localhost:8080/requests] : " +
            "Запрос на получение запросов пользователя с id: {}", userId);
        return ResponseEntity.ok(itemRequestService.findAllItemRequestByRequestorId(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestResponseDto>> getAllItemRequests(
        @RequestHeader("X-Sharer-User-Id") @NotNull Long userId,
        @Valid @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
        @Valid @RequestParam(defaultValue = "10") @Positive Integer size
    ) {
        log.info("GET [http://localhost:8080/requests] : " +
            "Запрос на получение всех запросов пользователем с id: {}", userId);
        return ResponseEntity.ok(itemRequestService.findAllItemRequests(userId, PageRequest.of(from / size, size,
            Sort.by(Sort.Order.desc("created")))));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestResponseDto> getItemRequestById(
        @RequestHeader("X-Sharer-User-Id") @NotNull Long userId,
        @PathVariable Long requestId
    ) {
        log.info("GET [http://localhost:8080/requests/{}] : " +
            "Запрос на получение запроса с id: {}, пользователем с id: {}", requestId, requestId, userId);
        return ResponseEntity.ok(itemRequestService.findItemRequestById(userId, requestId));
    }
}
