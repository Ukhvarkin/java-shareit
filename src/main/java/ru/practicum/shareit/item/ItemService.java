package ru.practicum.shareit.item;

import ru.practicum.shareit.item.comment.CommentDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto itemDto, Long userId);

    ItemDto updateItem(ItemDto itemDto, Long userId);

    ItemDto getItemById(Long itemId, Long userId);

    List<ItemDto> findAllItemsByOwner(Long userId, int from, int size);

    List<ItemDto> search(String text);

    CommentDto addComment(Long itemId, CommentDto comment, Long userId);

}
