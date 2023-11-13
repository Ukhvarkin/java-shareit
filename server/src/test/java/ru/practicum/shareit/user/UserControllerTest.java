package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserServiceImpl userService;

    @Nested
    @DisplayName("POST")
    public class MethodPost {
        @Test
        @DisplayName("Добавление нового пользователя")
        void methodPost_NewUserValidTest() throws Exception {
            UserDto userDtoToCreate = new UserDto(1L, "One", "one@yandex.ru");
            when(userService.addUser(userDtoToCreate)).thenReturn(userDtoToCreate);

            String result = mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userDtoToCreate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(userDtoToCreate), result);
        }
    }

    @Nested
    @DisplayName("PATCH")
    public class MethodPatch {
        @Test
        @DisplayName("Редактирование пользователя по id")
        void methodPatch_updateUserTest() throws Exception {
            Long userId = 1L;
            UserDto userDto = new UserDto(userId, "NewName", null);
            UserDto updatedUserDto = new UserDto(userId, "NewName", "old@yandex.ru");

            when(userService.updateUser(userId, userDto)).thenReturn(updatedUserDto);

            String result = mockMvc.perform(patch("/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(updatedUserDto), result);
        }

    }

    @Nested
    @DisplayName("GET")
    public class MethodGet {
        @Test
        @DisplayName("Получение списка всех пользователей")
        void methodGet_AllUsersTest() throws Exception {
            List<UserDto> users = new ArrayList<>();
            users.add(new UserDto(1L, "User1", "user1@example.com"));
            users.add(new UserDto(2L, "User2", "user2@example.com"));

            when(userService.findAllUsers()).thenReturn(users);

            mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(
                    jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("[0].id", is(1)))
                .andExpect(jsonPath("[0].name", is("User1")))
                .andExpect(jsonPath("[0].email", is("user1@example.com")))
                .andExpect(jsonPath("[1].id", is(2)))
                .andExpect(jsonPath("[1].name", is("User2")))
                .andExpect(jsonPath("[1].email", is("user2@example.com")));

            verify(userService).findAllUsers();
        }

        @Test
        @DisplayName("Получение пользователя по id")
        void methodGet_UserByIdTest() throws Exception {
            Long userId = 1L;

            mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk());

            verify(userService).findUserById(userId);
        }

    }

    @Nested
    @DisplayName("DELETE")
    public class MethodDelete {
        @Test
        @DisplayName("Удаление пользователя по id")
        void methodDelete_UserByIdTest() throws Exception {
            mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk())
            ;

            mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0))
            ;
        }
    }
}