package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private final UserRepository userRepository;
    private final HashMap<Long, List<Item>> items = new HashMap<>();
    private long nextId = 1;

    @Override
    public Item addItem(ItemDto itemDto, Long userId) {
        if (userRepository.userExists(userId)) {
            Long itemId = getId();
            Item item = new Item();
            item.setId(itemId);
            item.setName(itemDto.getName());
            item.setDescription(itemDto.getDescription());
            item.setAvailable(itemDto.getAvailable());
            item.setOwner(userId);

            items.compute(item.getOwner(), (ownerId, userItems) -> {
                if (userItems == null) {
                    userItems = new ArrayList<>();
                }
                userItems.add(item);
                return userItems;
            });
            log.info("Создана вещь: {}", item);
            return item;
        } else {
            throw new UserNotFoundException("Пользователь с id: " + userId);
        }
    }

    @Override
    public Item updateItem(Long itemId, ItemDto itemDto, Long userId) throws ItemNotFoundException {
        List<Item> userItems = items.get(userId);
        if (userItems != null) {
            for (Item item : userItems) {
                if (item.getId().equals(itemId)) {

                    if (itemDto.getName() != null) {
                        item.setName(itemDto.getName());
                        log.info("Редактирование имени: {}", itemDto.getName());
                    }
                    if (itemDto.getDescription() != null) {
                        item.setDescription(itemDto.getDescription());
                        log.info("Редактирование описания: {}", itemDto.getDescription());
                    }
                    if (itemDto.getAvailable() != null) {
                        item.setAvailable(itemDto.getAvailable());
                        log.info("Редактирование наличия: {}", itemDto.getAvailable());
                    }
                    log.info("Вещь отредактирована: {}", item);
                    return item;
                }
            }
        }
        throw new ItemNotFoundException("Вещь с id: " + itemId);
    }

    @Override
    public Item getItemById(Long itemId) {
        List<Item> allItems = items.values().stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        Optional<Item> foundItem = allItems.stream()
            .filter(item -> item.getId().equals(itemId))
            .findFirst();

        if (foundItem.isPresent()) {
            return foundItem.get();
        } else {
            throw new ItemNotFoundException("Вещь с id: " + itemId);
        }
    }

    @Override
    public List<Item> getAllItemsByOwner(Long userId) {
        List<Item> userItem = items.get(userId);
        return userItem != null ? new ArrayList<>(userItem) : Collections.emptyList();
    }

    @Override
    public List<Item> search(String text) {
        List<Item> allItems = items.values().stream()
            .flatMap(Collection::stream)
            .filter(Item::getAvailable)
            .collect(Collectors.toList());

        String textLowerCase = text.toLowerCase();

        if (!text.isEmpty()) {
            return allItems.stream()
                .filter(item -> (item.getName().toLowerCase().contains(textLowerCase)) ||
                    (item.getDescription().toLowerCase().contains(textLowerCase)))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public void deleteAll() {
        items.clear();
        nextId = 1;
    }

    private Long getId() {
        return nextId++;
    }
}

