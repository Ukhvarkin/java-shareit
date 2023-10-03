package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {
    User addUser(User user);

    User updateUser(Long userId, User user);

    User getUserById(Long userId);

    void deleteUserById(Long userId);

    List<User> getAllUsers();
}
