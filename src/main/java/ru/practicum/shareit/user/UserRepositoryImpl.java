package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.UserNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {
    private final List<User> users = new ArrayList<>();
    private long nextId = 1;

    @Override
    public User addUser(User user) {
        if (duplicateEmail(user.getEmail())) {
            throw new ConflictException("Пользователь с почтой: " +
                user.getEmail() + " уже существует.");
        } else {
            user.setId(getId());
            users.add(user);
            log.info("Создан пользователь: {}", user);
        }
        return user;
    }

    @Override
    public User updateUser(Long userId, User updateUser) {
        User existingUser = getUserById(userId);
        if (updateUser.getName() != null) {
            existingUser.setName(updateUser.getName());
            log.info("Редактирование имени: {}", updateUser.getName());
        }
        if (updateUser.getEmail() != null) {
            String newEmail = updateUser.getEmail();

            if (!newEmail.equals(existingUser.getEmail())) {
                if (!duplicateEmail(newEmail)) {
                    existingUser.setEmail(newEmail);
                    log.info("Редактирование почты: {}", updateUser.getEmail());
                } else {
                    throw new ConflictException("Пользователь с почтой: " +
                        updateUser.getEmail() + " уже существует.");
                }
            }
        }
        log.info("Пользователь отредактирован: {}", existingUser);
        return existingUser;
    }

    @Override
    public User getUserById(Long userId) {
        return users.stream()
            .filter(user -> user.getId().equals(userId))
            .findFirst()
            .orElseThrow(() -> new UserNotFoundException("Пользователь с id: " + userId));
    }

    @Override
    public void deleteUserById(Long userId) {
        if (userExists(userId)) {
            log.info("Пользователь с id: {} - удален", userId);
            users.removeIf(user -> user.getId().equals(userId));
        } else {
            throw new UserNotFoundException("Пользователь с id: " + userId);
        }

    }

    @Override
    public List<User> getAllUsers() {
        return users;
    }

    private Long getId() {
        return nextId++;
    }

    @Override
    public boolean userExists(Long userId) {
        return users.stream().anyMatch(user -> user.getId().equals(userId));
    }

    @Override
    public void deleteAll() {
        nextId = 1;
        users.clear();
    }

    private boolean duplicateEmail(String email) {
        return users.stream().anyMatch(user -> user.getEmail().equals(email));
    }
}
