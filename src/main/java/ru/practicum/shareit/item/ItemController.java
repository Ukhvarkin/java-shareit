package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemDto;

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
    public Item addItem(@RequestBody @Valid ItemDto itemDto,
                        @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("POST [http://localhost:8080/items] : " +
            "Запрос создания вещи: {}, userId: {}", itemDto, userId);
        return itemService.addItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public Item updateItem(@PathVariable Long itemId,
                           @RequestBody ItemDto itemDto,
                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("PATCH [http://localhost:8080/items/{}] : " +
            "Запрос редактирования вещи: {}, userId {}", itemId, itemDto, userId);
        return itemService.updateItem(itemId, itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public Item getItemById(@PathVariable Long itemId) {
        log.info("GET [http://localhost:8080/items/{}] : " +
            "Запрос вещи по id", itemId);
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<Item> getAllItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("http://localhost:8080/items : " +
            "Запрос списка всех вещей пользователя с id: {}", userId);
        return itemService.getAllItemsByOwner(userId);
    }

    @GetMapping("/search")
    public List<Item> search(@RequestParam String text) {
        log.info(
            "GET [http://localhost:8080/items/search?text={}] : " +
                "Запрос поиска вещей по названию и/или описанию",
            text
        );
        return itemService.search(text);
    }
}
