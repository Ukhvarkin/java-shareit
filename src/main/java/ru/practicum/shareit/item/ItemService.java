package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    Item addItem(ItemDto itemDto, Long userId);

    Item updateItem(Long itemId, ItemDto itemDto, Long userId);

    Item getItemById(Long itemId);

    List<Item> getAllItemsByOwner(Long userId);

    List<Item> search(String text);
}
