package ru.practicum.shareit.item;

import java.util.List;

public interface ItemRepository {
    Item addItem(Item item, Long userId);

    Item updateItem(Item item);

    Item getItemById(Long itemId);

    List<Item> getAllItemsByOwner(Long userId);

    List<Item> search(String text);

    void deleteAll();

    boolean containsIdItem(Long itemId);
}
