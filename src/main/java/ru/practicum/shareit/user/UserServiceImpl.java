package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.UserNotFoundException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User addUser(User user) {
        if (userRepository.duplicateEmail(user.getEmail())) {
            throw new ConflictException("Пользователь с почтой: " + user.getEmail() + " уже существует.");
        } else {
            return userRepository.addUser(user);
        }
    }


    @Override
    public User updateUser(Long userId, User user) {
        User existingUser = getUserById(userId);
        if (user.getName() != null) {
            existingUser.setName(user.getName());
            log.info("Редактирование имени: {}", user.getName());
        }
        if (user.getEmail() != null) {
            String newEmail = user.getEmail();
            if (!newEmail.equals(existingUser.getEmail())) {
                if (!userRepository.duplicateEmail(newEmail)) {
                    existingUser.setEmail(newEmail);
                    log.info("Редактирование почты: {}", user.getEmail());
                } else {
                    throw new ConflictException(
                        "Пользователь с почтой: " + user.getEmail() + " уже существует.");
                }
            }
        }
        return userRepository.updateUser(userId, existingUser);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public User getUserById(Long userId) {
        if (userRepository.containsUser(userId)) {
            return userRepository.getUserById(userId);
        } else {
            throw new UserNotFoundException("Пользователь с id: " + userId);
        }
    }

    @Override
    public void deleteUserById(Long userId) {
        if (userRepository.containsUser(userId)) {
            userRepository.deleteUserById(userId);
        } else {
            throw new UserNotFoundException("Пользователь с id: " + userId);
        }

    }

}
