package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.UserNotFoundException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerTest {

    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    @MockBean
    private UserService userService;

    UserController userController;

    private final UserDto userDto1 = UserDto.builder()
        .id(1L)
        .name("One")
        .email("one@yandex.ru")
        .build();

    private final UserDto userDto2 = UserDto.builder()
        .id(2L)
        .name("Two")
        .email("two@yandex.ru")
        .build();

    private final UserDto updateUserDto1 = UserDto.builder()
        .id(1L)
        .name("Update One")
        .email("upone@yandex.ru")
        .build();

    @Nested
    @DisplayName("POST")
    public class MethodPost {
        @Test
        @DisplayName("Добавление пользователя")
        public void methodPost_addUserTest() throws Exception {
            when(userService.addUser(ArgumentMatchers.any(UserDto.class))).thenReturn(userDto1);

            mockMvc.perform(post("/users")
                    .content(objectMapper.writeValueAsString(userDto1))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(userDto1)));

            verify(userService, times(1)).addUser(ArgumentMatchers.any(UserDto.class));
        }

        @Test
        @DisplayName("Добавление нового пользователя - name : empty")
        public void methodPost_NewUserValidTrue_NameEmptyTest() throws Exception {
            userDto1.setName(null);

            mockMvc.perform(post("/users")
                    .content(objectMapper.writeValueAsString(userDto1))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Добавление нового пользователя - email : empty")
        public void methodPost_NewUserValidTrue_EmailEmptyTest() throws Exception {
            userDto1.setEmail(null);

            mockMvc.perform(post("/users")
                    .content(objectMapper.writeValueAsString(userDto1))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError());
        }

        @Nested
        @DisplayName("PATCH")
        public class MethodPatch {
            @Test
            @DisplayName("Редактирование пользователя по id")
            public void methodPatch_updateUserTest() throws Exception {
                when(userService.updateUser(ArgumentMatchers.eq(updateUserDto1.getId()), any(UserDto.class)))
                    .thenReturn(updateUserDto1);

                mockMvc.perform(patch("/users/1")
                        .content(objectMapper.writeValueAsString(updateUserDto1))
                        .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(updateUserDto1)));
                ;

                verify(userService, times(1))
                    .updateUser(ArgumentMatchers.eq(updateUserDto1.getId()), ArgumentMatchers.any(UserDto.class));
            }
        }

        @Nested
        @DisplayName("GET")
        public class MethodGet {
            @Test
            @DisplayName("Получение списка всех пользователей")
            public void methodGet_AllUsersTest() throws Exception {
                when(userService.findAllUsers()).thenReturn(List.of(userDto1, userDto2));

                mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
            }

            @Test
            @DisplayName("Получение пользователя по id")
            public void methodGet_UserByIdTest() throws Exception {
                when(userService.findUserById(userDto1.getId())).thenReturn(userDto1);

                mockMvc.perform(get("/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
            }

            @Test
            @DisplayName("Запрос пользователя: ID -1")
            public void methodGet_UserIdMinus1Test() throws Exception {
                when(userService.findUserById(-1L)).thenThrow(UserNotFoundException.class);

                mockMvc.perform(get("/users/-1"))
                    .andExpect(status().is4xxClientError());
            }

            @Test
            @DisplayName("Запрос пользователя: ID 9999")
            public void methodGet_UserId9999Test() throws Exception {
                when(userService.findUserById(9999L)).thenThrow(UserNotFoundException.class);

                mockMvc.perform(get("/users/9999"))
                    .andExpect(status().is4xxClientError());
            }

        }

        @Nested
        @DisplayName("DELETE")
        public class MethodDelete {
            @Test
            @DisplayName("Удаление пользователя по id")
            public void methodDelete_UserByIdTest() throws Exception {
                mockMvc.perform(delete("/users/1"))
                    .andExpect(status().isOk());

                verify(userService, times(1)).deleteUserById(1L);
            }
        }
    }
}