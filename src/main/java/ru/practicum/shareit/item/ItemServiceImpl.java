package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    @Override
    public Item addItem(ItemDto itemDto, Long userId) {
        itemDtoValidation(itemDto);
        return itemRepository.addItem(itemDto, userId);
    }

    @Override
    public Item updateItem(Long itemId, ItemDto itemDto, Long userId) {
        return itemRepository.updateItem(itemId, itemDto, userId);
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemRepository.getItemById(itemId);
    }

    @Override
    public List<Item> getAllItemsByOwner(Long userId) {
        return itemRepository.getAllItemsByOwner(userId);
    }

    @Override
    public List<Item> search(String text) {
        return itemRepository.search(text);
    }

    private void itemDtoValidation(ItemDto itemDto) {
        if (itemDto == null) {
            String message = "Некорректный ввод. Пустой объект.";
            log.warn(message);
            throw new ValidationException(message);
        }
        if (itemDto.getName() == null) {
            String message = "Некорректный ввод, пустое поле имени.";
            log.warn(message);
            throw new ValidationException(message);
        }
        if (itemDto.getDescription() == null) {
            String message = "Некорректный ввод, пустое поле описания.";
            log.warn(message);
            throw new ValidationException(message);
        }
        if (itemDto.getAvailable() == null) {
            String message = "Некорректный ввод, пустое поле наличия вещи.";
            log.warn(message);
            throw new ValidationException(message);
        }
    }

}
