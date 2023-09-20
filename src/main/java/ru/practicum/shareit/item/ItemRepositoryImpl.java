package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private final UserRepository userRepository;
    private final HashMap<Long, Item> items = new HashMap<>();
    private long nextId = 1;

    @Override
    public Item addItem(Item item, Long userId) {
        Long itemId = getId();
        item.setId(itemId);
        items.put(itemId, item);
        log.info("Создана вещь: {}", item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        return items.put(item.getId(), item);
    }

    @Override
    public Item getItemById(Long itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> getAllItemsByOwner(Long userId) {
        return items.values().stream()
            .filter(item -> item.getOwner().equals(userId))
            .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text) {
        List<Item> allItems = items.values().stream()
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

    @Override
    public boolean containsIdItem(Long itemId) {
        return items.containsKey(itemId);
    }

    private Long getId() {
        return nextId++;
    }


}

