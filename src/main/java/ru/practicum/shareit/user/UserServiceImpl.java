package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto addUser(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().length() == 0) {
            throw new ValidationException("Некорректный ввод. Пустое поле email.");
        }
        User user = userMapper.toUser(userDto);
        return userMapper.toUserDto(userRepository.save(user));
    }


    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Не существует пользователь с id: " + userId));

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
            log.info("Редактирование имени: {}", userDto.getName());
        }
        if (userDto.getEmail() != null) {
            log.info("Редактирование почты: {}", userDto.getEmail());
            existingUser.setEmail(userDto.getEmail());
        }
        return userMapper.toUserDto(userRepository.save(existingUser));
    }

    @Override
    public List<UserDto> findAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto> result = new ArrayList<>();
        for (User user :
            users) {
            result.add(userMapper.toUserDto(user));
        }
        return result;
    }

    @Override
    public UserDto findUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Не найден пользователь с id: " + userId));
        return userMapper.toUserDto(user);
    }

    @Override
    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }
}
