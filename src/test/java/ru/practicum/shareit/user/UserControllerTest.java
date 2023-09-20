package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

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
    @Autowired
    private UserRepositoryImpl userRepository;

    @BeforeEach
    public void beforeEach() {
        createUser();
    }

    @AfterEach
    public void afterEach(){
        userRepository.deleteAll();
    }

    private void createUser() {
        User user = new User();
        user.setName("name");
        user.setEmail("mail@yandex.ru");
        userRepository.addUser(user);
    }

    @Nested
    @DisplayName("POST")
    public class MethodPost {
        @Test
        @DisplayName("Добавление нового пользователя - name : empty")
        public void methodPost_NewUserValidTrue_NameEmptyTest() throws Exception {
            UserDto userDto = UserDto.builder()
                .name("")
                .email("mail@mail.ru")
                .build();

            mockMvc.perform(post("/users")
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
            ;
        }

        @Test
        @DisplayName("Добавление нового пользователя - email : empty")
        public void methodPost_NewUserValidTrue_EmailEmptyTest() throws Exception {
            UserDto userDto = UserDto.builder()
                .name("name")
                .email("")
                .build();

            mockMvc.perform(post("/users")
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
            ;
        }

    }

    @Nested
    @DisplayName("PATCH")
    public class MethodPatch {
        @Test
        @DisplayName("Редактирование пользователя по id")
        public void methodPatch_updateUserTest() throws Exception {
            UserDto updateUser = UserDto.builder()
                .name("update name")
                .email("update@yandex.ru")
                .build();

            mockMvc.perform(patch("/users/1")
                .content(objectMapper.writeValueAsString(updateUser))
                .contentType(MediaType.APPLICATION_JSON));

            mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("update name"))
                .andExpect(jsonPath("$.email").value("update@yandex.ru"))
            ;
        }
    }

    @Nested
    @DisplayName("GET")
    public class MethodGet {
        @Test
        @DisplayName("Получение списка всех пользователей")
        public void methodGet_AllUsersTest() throws Exception {
            mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
            ;
        }

        @Test
        @DisplayName("Получение пользователя по id")
        public void methodGet_UserByIdTest() throws Exception {
            mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
            ;
        }

        @Test
        @DisplayName("Запрос пользователя: ID -1")
        public void methodGet_UserIdMinus1Test() throws Exception {
            mockMvc.perform(get("/users/-1"))
                .andExpect(status().is4xxClientError())
            ;
        }

        @Test
        @DisplayName("Запрос пользователя: ID 9999")
        public void methodGet_UserId9999Test() throws Exception {
            mockMvc.perform(get("/users/9999"))
                .andExpect(status().is4xxClientError())
            ;
        }

    }

    @Nested
    @DisplayName("DELETE")
    public class MethodDelete {
        @Test
        @DisplayName("Удаление пользователя по id")
        public void methodDelete_UserByIdTest() throws Exception {
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