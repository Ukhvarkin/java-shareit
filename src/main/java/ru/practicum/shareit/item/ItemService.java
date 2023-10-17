package ru.practicum.shareit.item;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.comment.CommentDto;

import java.util.List;

public interface ItemService {
    ItemResponseDto addItem(ItemResponseDto itemDto, Long userId);

    ItemResponseDto updateItem(ItemResponseDto itemDto, Long userId);

    ItemResponseDto getItemById(Long itemId, Long userId);

    List<ItemResponseDto> findAllItemsByOwner(Long userId, int from, int size);

    List<ItemResponseDto> search(String text, Pageable pageable);

    CommentDto addComment(Long itemId, CommentDto comment, Long userId);

}
