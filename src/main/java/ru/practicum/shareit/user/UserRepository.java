package ru.practicum.shareit.user;

import java.util.List;

public interface UserRepository {
    User addUser(User user);

    User updateUser(Long userId, User user);

    User getUserById(Long userId);

    void deleteUserById(Long userId);

    void deleteAll();

    List<User> getAllUsers();

    boolean userExists(Long userId);
}
