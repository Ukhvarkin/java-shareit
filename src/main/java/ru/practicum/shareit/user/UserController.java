package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public User addUser(@RequestBody @Valid User user) {
        log.info("POST [http://localhost:8080/users] : " +
            "Запрос добавления пользователя: {}", user);
        return userService.addUser(user);
    }

    @PatchMapping("/{userId}")
    public User updateUser(@PathVariable Long userId,
                           @RequestBody User user) {
        log.info("PATCH [http://localhost:8080/users/{}] : " +
            "Запрос редактирования пользователя с id: {} : {}", userId, userId, user);
        return userService.updateUser(userId, user);
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        log.info("GET [http://localhost:8080/users/{}] : " +
            "Запрос получения пользователя по id", userId);
        return userService.getUserById(userId);
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("GET [http://localhost:8080/users] : " +
            "Запрос списка всех пользователей");
        return userService.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        log.info("DELETE [http://localhost:8080/users/{}] : " +
            "Запрос удаления пользователя по id", userId);
        userService.deleteUserById(userId);
    }
}
