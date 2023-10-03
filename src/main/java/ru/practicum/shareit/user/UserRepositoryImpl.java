package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public User addUser(User user) {
        user.setId(getId());
        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @Override
    public User updateUser(Long userId, User user) {
        users.put(userId, user);
        log.info("Пользователь отредактирован: {}", user);
        return user;
    }

    @Override
    public User getUserById(Long userId) {
        log.info("Пользователь c id : {} , найден", userId);
        return users.get(userId);
    }

    @Override
    public void deleteUserById(Long userId) {
        log.info("Пользователь c id : {} , удален", userId);
        users.remove(userId);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    private Long getId() {
        return nextId++;
    }

    @Override
    public boolean containsUser(Long userId) {
        return users.containsKey(userId);
    }

    @Override
    public void deleteAll() {
        nextId = 1;
        users.clear();
    }

    @Override
    public boolean duplicateEmail(String email) {
        return users.values().stream()
            .anyMatch(user -> user.getEmail().equals(email));
    }
}
