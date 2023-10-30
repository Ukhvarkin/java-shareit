package ru.practicum.shareit.item;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestBody ItemResponseDto itemDto,
                                          @RequestHeader("X-Sharer-User-Id") @NonNull Long userId) {
        log.info("Запрос создания вещи: {}, userId: {}", itemDto, userId);
        return itemClient.addItem(itemDto, userId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@PathVariable Long itemId,
                                             @RequestBody CommentDto commentDto,
                                             @RequestHeader("X-Sharer-User-Id") @NonNull Long userId) {
        log.info("Запрос создания комментария: {},itemId {}, userId: {}", itemId, commentDto, userId);

        return itemClient.addComment(itemId, commentDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@PathVariable Long itemId,
                                             @RequestBody ItemResponseDto itemDto,
                                             @RequestHeader("X-Sharer-User-Id") @NonNull Long userId) {
        log.info("Запрос редактирования вещи c id:{}, item {}, userId {}", itemId, itemDto, userId);
        return itemClient.updateItem(itemId, itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long itemId) {
        log.info("Запрос вещи по id: {}", itemId);

        return itemClient.getItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItems(
        @RequestHeader("X-Sharer-User-Id") @NonNull Long userId,
        @RequestParam(defaultValue = "0") @PositiveOrZero int from,
        @RequestParam(defaultValue = "10") @Positive int size
    ) {
        log.info("Запрос списка всех вещей пользователя с id: {}, from: {}, size: {}", userId, from, size);
        return itemClient.findAllItemsByOwner(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(
        @RequestHeader("X-Sharer-User-Id") @NonNull Long userId,
        @RequestParam String text,
        @RequestParam(defaultValue = "0") @PositiveOrZero int from,
        @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Запрос поиска вещей по названию и/или описанию: {}", text);
        return itemClient.search(userId, text, from, size);
    }
}
