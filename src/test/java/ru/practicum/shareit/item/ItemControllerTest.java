package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemControllerTest {
    @Mock
    private ItemService itemService;
    @InjectMocks
    private ItemController itemController;

    private int from;
    private int size;

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

    private final ItemResponseDto item = ItemResponseDto.builder()
        .id(1L)
        .name("item")
        .description("description")
        .available(true)
        .ownerId(userDto1.getId())
        .requestId(null)
        .build();

    private final ItemResponseDto itemUpdate = ItemResponseDto.builder()
        .id(1L)
        .name("item update")
        .description("description update")
        .available(false)
        .ownerId(userDto1.getId())
        .requestId(null)
        .build();

    private final CommentDto comment = CommentDto.builder()
        .id(1L)
        .text("comment text")
        .authorName(userDto2.getName())
        .created(LocalDateTime.now())
        .build();

    @BeforeEach
    public void beforeEach() {
        from = 0;
        size = 10;
    }

    @Nested
    @DisplayName("POST")
    public class MethodPost {
        @Test
        @DisplayName("Создание вещи")
        public void methodPost_createItemTest() {
            when(itemService.addItem(item, userDto1.getId())).thenReturn(item);
            ResponseEntity<ItemResponseDto> response = itemController.addItem(item, userDto1.getId());
            assertEquals(OK, response.getStatusCode());
            assertEquals(Objects.requireNonNull(response.getBody()).getId(), item.getId());
        }

        @Test
        @DisplayName("Создание вещи - name : null")
        public void methodPost_createItemEmptyNameTest() {
            item.setName(null);
            when(itemService.addItem(item, userDto1.getId())).thenThrow(ValidationException.class);
            assertThrows(ValidationException.class, () -> {
                itemController.addItem(item, userDto1.getId());
            });
        }

        @Test
        @DisplayName("Создание комментария")
        public void methodPost_createCommentTest() {
            when(itemService.addComment(item.getId(), comment, userDto2.getId())).thenReturn(comment);
            ResponseEntity<CommentDto> response = itemController.addComment(item.getId(), comment, userDto2.getId());
            assertEquals(OK, response.getStatusCode());
            assertEquals(Objects.requireNonNull(response.getBody()).getId(), comment.getId());
            assertEquals(Objects.requireNonNull(response.getBody()).getText(), comment.getText());
            assertEquals(Objects.requireNonNull(response.getBody()).getAuthorName(), comment.getAuthorName());
        }

        @Test
        @DisplayName("Создание комментария - text : null")
        public void methodPost_createCommentEmptyDescriptionTest() {
            comment.setText(null);
            when(itemService.addComment(item.getId(), comment, userDto1.getId())).thenThrow(ValidationException.class);
            assertThrows(ValidationException.class, () -> {
                itemController.addComment(item.getId(), comment, userDto1.getId());
            });
        }
    }

    @Nested
    @DisplayName("PATCH")
    public class MethodPatch {
        @Test
        @DisplayName("Редактирование вещи")
        public void methodPatch_updateItemTest() {
            when(itemService.updateItem(itemUpdate, userDto1.getId())).thenReturn(itemUpdate);

            ResponseEntity<ItemResponseDto> response = itemController.updateItem(
                item.getId(),
                itemUpdate,
                userDto1.getId()
            );

            assertEquals(OK, response.getStatusCode());
            assertEquals(itemUpdate.getName(), Objects.requireNonNull(response.getBody()).getName());
            assertEquals(itemUpdate.getDescription(), Objects.requireNonNull(response.getBody()).getDescription());
            assertEquals(itemUpdate.getAvailable(), Objects.requireNonNull(response.getBody()).getAvailable());
        }

        @Test
        @DisplayName("Редактирование вещи - description : null")
        public void methodPatch_validFalse_ItemNameNullTest() {
            itemUpdate.setName(null);

            when(itemService.updateItem(itemUpdate, userDto1.getId())).thenThrow(ValidationException.class);
            assertThrows(ValidationException.class, () -> {
                itemController.updateItem(item.getId(), itemUpdate, userDto1.getId());
            });
        }

        @Test
        @DisplayName("Редактирование вещи - name : null")
        public void methodPatch_ValidFalse_ItemDescriptionNullTest() {
            itemUpdate.setDescription(null);

            when(itemService.updateItem(itemUpdate, userDto1.getId())).thenThrow(ValidationException.class);
            assertThrows(ValidationException.class, () -> {
                itemController.updateItem(item.getId(), itemUpdate, userDto1.getId());
            });
        }
    }

    @Nested
    @DisplayName("GET")
    public class MethodGet {
        @Test
        @DisplayName("Получение вещи по id")
        public void methodGet_itemByIdTest() {
            when(itemService.getItemById(1L, userDto1.getId()))
                .thenReturn(item);

            ResponseEntity<ItemResponseDto> response = itemController.getItemById(userDto1.getId(), 1L);
            assertEquals(OK, response.getStatusCode());
            assertEquals(item.getName(), Objects.requireNonNull(response.getBody()).getName());
            assertEquals(item.getDescription(), Objects.requireNonNull(response.getBody()).getDescription());
        }

        @Test
        @DisplayName("Получение вещи по несуществующему id: 9999")
        public void methodGet_itemById9999Test() {
            when(itemService.getItemById(9999L, userDto1.getId())).thenThrow(ItemNotFoundException.class);
            assertThrows(ItemNotFoundException.class, () -> {
                itemController.getItemById(userDto1.getId(), 9999L);
            });
        }

        @Test
        @DisplayName("Получение списка вещей пользователя")
        public void methodGet_allItemsTest() {
            when(itemService.findAllItemsByOwner(userDto1.getId(), from, size))
                .thenReturn(List.of(item));

            ResponseEntity<List<ItemResponseDto>> response = itemController.getAllItems(userDto1.getId(), from, size);
            assertEquals(OK, response.getStatusCode());
            assertEquals(1, Objects.requireNonNull(response.getBody()).size());
        }

        @Test
        @DisplayName("Получение списка вещей пользователя : по несуществующему пользователю")
        public void methodGet_allItemsByUserId9999Test() {
            when(itemService.getItemById(item.getId(), 9999L)).thenThrow(UserNotFoundException.class);
            assertThrows(UserNotFoundException.class, () -> {
                itemController.getItemById(9999L, item.getId());
            });
        }

        @Test
        @DisplayName("Получение списка вещей пользователя")
        public void methodGet_searchTest() {
            Pageable pageable = PageRequest.of(from / size, size);

            when(itemService.search("IT", pageable)).thenReturn(List.of(item));

            ResponseEntity<List<ItemResponseDto>> response = itemController.search("IT", from, size);
            assertEquals(OK, response.getStatusCode());
            assertEquals(1, Objects.requireNonNull(response.getBody()).size());
        }
    }
}