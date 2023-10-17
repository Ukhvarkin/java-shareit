package ru.practicum.shareit.item;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.comment.CommentDto;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponseDto> addItem(@RequestBody @Valid ItemResponseDto itemDto,
                                                   @RequestHeader("X-Sharer-User-Id") @NonNull Long userId) {
        log.info("POST [http://localhost:8080/items] : " +
            "Запрос создания вещи: {}, userId: {}", itemDto, userId);
        return ResponseEntity.ok(itemService.addItem(itemDto, userId));
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(@PathVariable Long itemId,
                                                 @RequestBody CommentDto commentDto,
                                                 @RequestHeader("X-Sharer-User-Id") @NonNull Long userId) {
        log.info("POST [http://localhost:8080/items/{}/comment] : " +
            "Запрос создания комментария: {}, userId: {}", itemId, commentDto, userId);

        return ResponseEntity.ok(itemService.addComment(itemId, commentDto, userId));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> updateItem(@PathVariable Long itemId,
                                                      @RequestBody ItemResponseDto itemDto,
                                                      @RequestHeader("X-Sharer-User-Id") @NonNull Long userId) {
        log.info("PATCH [http://localhost:8080/items/{}] : " +
            "Запрос редактирования вещи: {}, userId {}", itemId, itemDto, userId);
        itemDto.setId(itemId);
        return ResponseEntity.ok(itemService.updateItem(itemDto, userId));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @PathVariable Long itemId) {
        log.info("GET [http://localhost:8080/items/{}] : " +
            "Запрос вещи по id", itemId);

        return ResponseEntity.ok(itemService.getItemById(itemId, userId));
    }

    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getAllItems(
        @RequestHeader("X-Sharer-User-Id") @NonNull Long userId,
        @RequestParam(defaultValue = "0") int from,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.info("GET [http://localhost:8080/items] : " +
            "Запрос списка всех вещей пользователя с id: {}, from: {}, size: {}", userId, from, size);
        List<ItemResponseDto> items = itemService.findAllItemsByOwner(userId, from, size);

        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemResponseDto>> search(
        @RequestParam String text,
        @RequestParam(defaultValue = "0") int from,
        @RequestParam(defaultValue = "10") int size) {
        log.info(
            "GET [http://localhost:8080/items/search?text={}] : " +
                "Запрос поиска вещей по названию и/или описанию",
            text
        );
        return ResponseEntity.ok(itemService.search(text, PageRequest.of(from / size, size)));
    }
}
