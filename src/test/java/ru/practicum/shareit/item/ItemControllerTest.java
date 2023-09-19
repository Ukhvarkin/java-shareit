package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepositoryImpl;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepositoryImpl userRepository;
    @Autowired
    private ItemRepositoryImpl itemRepository;

    @BeforeEach
    public void beforeEach() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        Long userId = createUser();
        createItem(userId);
    }

    private Long createUser() {
        User user = new User();
        user.setName("name");
        user.setEmail("mail@yandex.ru");
        return userRepository.addUser(user).getId();
    }

    private void createItem(Long userId) {
        ItemDto itemDto = ItemDto.builder()
            .name("Item name")
            .description("Item description")
            .available(true)
            .build();
        itemRepository.addItem(itemDto, userId);
    }

    @Nested
    @DisplayName("POST")
    public class MethodPost {
        @Test
        @DisplayName("Создание вещи")
        public void methodPost_createItemTest() throws Exception {

            ItemDto itemDto = ItemDto.builder()
                .name("name")
                .description("description")
                .available(true)
                .build();

            mockMvc.perform(post("/items")
                    .header("X-Sharer-User-Id", "1")
                    .content(objectMapper.writeValueAsString(itemDto))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
            ;
        }
    }

    @Nested
    @DisplayName("PATCH")
    public class MethodPatch {
        @Test
        @DisplayName("Редактирование вещи по id")
        public void methodPatch_updateUserTest() throws Exception {
            ItemDto itemDto = ItemDto.builder()
                .name("update")
                .description("update description")
                .available(true)
                .build();
            mockMvc.perform(patch("/items/1")
                    .header("X-Sharer-User-Id", "1")
                    .content(objectMapper.writeValueAsString(itemDto))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
            ;

            mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("update"))
                .andExpect(jsonPath("$.description").value("update description"))
                .andExpect(jsonPath("$.available").value(true))
            ;
        }

        @Test
        @DisplayName("Редактирование вещи - name : null")
        public void methodPatch_UserValidFalse_NameNullTest() throws Exception {
            ItemDto itemDto = ItemDto.builder()
                .name(null)
                .description("description")
                .available(true)
                .build();

            mockMvc.perform(put("/items")
                    .header("X-Sharer-User-Id", "1")
                    .content(objectMapper.writeValueAsString(itemDto))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
            ;
        }

        @Test
        @DisplayName("Редактирование вещи - description : null")
        public void methodPatch_UserValidFalse_DescriptionNullTest() throws Exception {
            ItemDto itemDto = ItemDto.builder()
                .name("name")
                .description(null)
                .available(true)
                .build();

            mockMvc.perform(put("/items")
                    .header("X-Sharer-User-Id", "1")
                    .content(objectMapper.writeValueAsString(itemDto))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
            ;
        }

        @Test
        @DisplayName("Редактирование вещи - description : null")
        public void methodPatch_UserValidFalse_isAvailableNullTest() throws Exception {
            ItemDto itemDto = ItemDto.builder()
                .name("name")
                .description("description")
                .available(null)
                .build();

            mockMvc.perform(put("/items")
                    .header("X-Sharer-User-Id", "1")
                    .content(objectMapper.writeValueAsString(itemDto))
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
            ;
        }
    }

    @Nested
    @DisplayName("GET")
    public class MethodGet {
        @Test
        @DisplayName("Запрос вещи по id: 1")
        public void methodGet_itemByIdTest() throws Exception {
            mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Item name"))
                .andExpect(jsonPath("$.description").value("Item description"))
                .andExpect(jsonPath("$.available").value(true))
            ;
        }

        @Test
        @DisplayName("Запрос списка всех вещей")
        public void methodGet_AllItemsTest() throws Exception {
            mockMvc.perform(get("/items")
                    .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
            ;
        }

        @Test
        @DisplayName("Поиск вещи по названию/описанию: nam")
        public void methodGet_SearchItemTest() throws Exception {
            mockMvc.perform(get("/items/search?text=nam"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
            ;
        }
    }
}