package ru.practicum.shareit.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class UserMapperTest {
    @InjectMocks
    private UserMapperImpl userMapper;
    private final User user = User.builder()
        .id(1L)
        .name("One")
        .email("one@yandex.ru")
        .build();
    private final UserDto userDto = UserDto.builder()
        .id(1L)
        .name("One")
        .email("one@yandex.ru")
        .build();

    @Nested
    @DisplayName("Маппинг в UserDto")
    class ToUserDtoTest {
        @Test
        public void shouldReturnUserDto() {
            UserDto userDto = userMapper.toUserDto(user);

            assertEquals(user.getName(), userDto.getName());
        }

        @Test
        public void shouldReturnNull() {
            UserDto result = userMapper.toUserDto(null);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Маппинг в User")
    class ToUserTest {
        @Test
        public void shouldReturnUser() {
            User user = userMapper.toUser(userDto);

            assertEquals(userDto.getName(), user.getName());
        }

        @Test
        public void shouldReturnNull() {
            User result = userMapper.toUser(null);

            assertNull(result);
        }
    }


}