package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {
    UserDto addUser(UserDto userDto);

    UserDto updateUser(Long userId, UserDto userDto);

    UserDto findUserById(Long userId);

    void deleteUserById(Long userId);

    List<UserDto> findAllUsers();
}
