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
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto addUser(@RequestBody @Valid UserDto userDto) {
        log.info("POST [http://localhost:8080/users] : Запрос добавления пользователя: {}", userDto);
        return userService.addUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable Long userId,
                              @RequestBody UserDto userDto) {
        log.info("PATCH [http://localhost:8080/users/{}] : Запрос редактирования пользователя с id: {} : {}", userId,
            userId, userDto);
        return userService.updateUser(userId, userDto);
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        log.info("GET [http://localhost:8080/users/{}] : Запрос получения пользователя по id", userId);
        return userService.findUserById(userId);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("GET [http://localhost:8080/users] : Запрос списка всех пользователей");
        return userService.findAllUsers();
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        log.info("DELETE [http://localhost:8080/users/{}] : Запрос удаления пользователя по id", userId);
        userService.deleteUserById(userId);
    }
}
