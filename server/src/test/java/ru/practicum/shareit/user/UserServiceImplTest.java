package ru.practicum.shareit.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Nested
    @DisplayName("Добавление нового пользователя")
    class CreateUserTest {
        @Test
        void shouldCreatedUser() {
            UserDto validUserDto = new UserDto(1L, "One", "one@yandex.ru");
            User user = new User(1L, "One", "one@yandex.ru");
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUser(validUserDto)).thenReturn(user);
            when(userMapper.toUserDto(user)).thenReturn(validUserDto);

            UserDto actualUserDto = userService.addUser(validUserDto);

            assertEquals(validUserDto, actualUserDto);
            verify(userRepository).save(user);
            verify(userMapper).toUser(validUserDto);
            verify(userMapper).toUserDto(user);
        }

        @Test
        void shouldReturnedThrowValidationException_IfEmptyEmail() {
            UserDto emptyEmailUserDto = new UserDto(1L, "One", "");

            ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.addUser(emptyEmailUserDto));

            assertEquals("Некорректный ввод. Пустое поле email.", exception.getMessage());
            verify(userRepository, never()).save(userMapper.toUser(emptyEmailUserDto));
            verify(userRepository, times(0)).save(userMapper.toUser(emptyEmailUserDto));
        }

        @Test
        void shouldReturnedThrowValidationException_IfNullEmail() {
            UserDto emptyEmailUserDto = new UserDto(1L, "One", null);

            ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.addUser(emptyEmailUserDto));

            assertEquals("Некорректный ввод. Пустое поле email.", exception.getMessage());
            verify(userRepository, never()).save(userMapper.toUser(emptyEmailUserDto));
            verify(userRepository, times(0)).save(userMapper.toUser(emptyEmailUserDto));
        }
    }

    @Nested
    @DisplayName("Редактирование пользователя")
    class UpdateUserTest {
        @Test
        void shouldUpdateUserName() {
            Long userId = 1L;
            UserDto userDto = new UserDto(userId, "NewName", null);
            User oldUser = new User(userId, "OldName", "old@yandex.ru");
            when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));

            UserDto updatedUserDto = userService.updateUser(userId, userDto);

            verify(userRepository).save(userArgumentCaptor.capture());
            User savedUser = userArgumentCaptor.getValue();

            assertEquals("NewName", savedUser.getName());
            assertEquals("old@yandex.ru", savedUser.getEmail());
        }

        @Test
        void shouldUpdateUserEmail() {
            Long userId = 1L;
            UserDto userDto = new UserDto(userId, null, "new@yandex.ru");
            User oldUser = new User(userId, "OldName", "old@yandex.ru");
            when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));

            UserDto updatedUserDto = userService.updateUser(userId, userDto);

            verify(userRepository).findById(userId);
            verify(userRepository).save(userArgumentCaptor.capture());
            User savedUser = userArgumentCaptor.getValue();

            assertEquals("new@yandex.ru", savedUser.getEmail());
            assertEquals("OldName", savedUser.getName());
        }

        @Test
        void shouldNotUpdateUserIfNotExists() {
            Long userId = 1L;
            UserDto userDto = new UserDto(userId, "NewName", "new@yandex.ru");
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, userDto));

            verify(userRepository).findById(userId);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Получение всех пользователей")
    class FindAllTest {
        @Test
        void shouldFindAllUsers() {
            List<User> users = new ArrayList<>();
            users.add(new User(1L, "One", "one@yandex.ru"));
            users.add(new User(2L, "Two", "two@yandex.rum"));

            List<UserDto> expectedUserDtos = new ArrayList<>();
            for (User user : users) {
                expectedUserDtos.add(new UserDto(user.getId(), user.getName(), user.getEmail()));
            }

            when(userRepository.findAll()).thenReturn(users);
            for (int i = 0; i < users.size(); i++) {
                when(userMapper.toUserDto(users.get(i))).thenReturn(expectedUserDtos.get(i));
            }

            List<UserDto> actualUserDtoList = userService.findAllUsers();

            assertEquals(expectedUserDtos.size(), actualUserDtoList.size());
            for (int i = 0; i < expectedUserDtos.size(); i++) {
                assertEquals(expectedUserDtos.get(i), actualUserDtoList.get(i));
            }
        }

        @Test
        void shouldReturnEmptyListIfNoUsersFound() {
            List<User> users = new ArrayList<>();
            when(userRepository.findAll()).thenReturn(users);

            List<UserDto> actualUserDtoList = userService.findAllUsers();

            assertTrue(actualUserDtoList.isEmpty());
        }
    }

    @Nested
    @DisplayName("Получение пользователя по id")
    class FindUserByIdTest {
        @Test
        void shouldReturnedUser() {
            Long userId = 1L;
            User expectedUser = new User();
            UserDto expectedUserDto = new UserDto();
            when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));
            when(userMapper.toUserDto(expectedUser)).thenReturn(expectedUserDto);

            UserDto actualUserDto = userService.findUserById(userId);

            assertEquals(expectedUserDto, actualUserDto);
        }

        @Test
        void shouldReturnedThrowUserNotFoundException() {
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.findUserById(userId));

            assertEquals("Не найден пользователь с id: 1", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Удаление пользователя")
    class DeleteUserByIdTest {
        @Test
        void shouldDeleteUserById() {
            Long userId = 1L;

            userService.deleteUserById(userId);

            verify(userRepository).deleteById(userId);
            verify(userRepository, times(1)).deleteById(userId);
        }
    }
}