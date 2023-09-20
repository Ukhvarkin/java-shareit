package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Item addItem(ItemDto itemDto, Long userId) {
        itemDtoValidation(itemDto);
        if (userRepository.containsUser(userId)) {
            Item item = new Item();
            item.setName(itemDto.getName());
            item.setDescription(itemDto.getDescription());
            item.setAvailable(itemDto.getAvailable());
            item.setOwner(userId);
            return itemRepository.addItem(item, userId);
        } else {
            throw new UserNotFoundException("Пользователь с id: " + userId);
        }
    }

    @Override
    public Item updateItem(ItemDto itemDto, Long userId) {
        Item existingItem = getItemById(itemDto.getId());
        if (!existingItem.getOwner().equals(userId)) {
            throw new UserNotFoundException("id: " + userId);
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
            log.info("Редактирование имени: {}", itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
            log.info("Редактирование описания: {}", itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
            log.info("Редактирование наличия: {}", itemDto.getAvailable());
        }
        return itemRepository.updateItem(existingItem);
    }

    @Override
    public Item getItemById(Long itemId) {
        if (itemRepository.containsIdItem(itemId)) {
            return itemRepository.getItemById(itemId);
        } else {
            throw new ItemNotFoundException("Вещь с id: " + itemId);
        }
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
