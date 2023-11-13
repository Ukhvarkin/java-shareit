package ru.practicum.shareit.item.comment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class CommentMapperTest {
    @InjectMocks
    private CommentMapperImpl commentMapper;

    @Nested
    @DisplayName("Маппинг в CommentDto")
    class ToCommentDto {
        @Test
        public void shouldReturnCommentDto() {
            Comment comment = new Comment();
            comment.setId(1L);
            comment.setText("Test comment");

            CommentDto result = commentMapper.toCommentDto(comment);

            assertEquals(comment.getId(), result.getId());
            assertEquals(comment.getText(), result.getText());
        }

        @Test
        public void shouldReturnNull() {
            CommentDto result = commentMapper.toCommentDto(null);
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Маппинг в Comment")
    class ToComment {
        @Test
        public void shouldReturnComment() {
            CommentDto commentDto = new CommentDto();
            commentDto.setId(1L);
            commentDto.setText("comment");

            Item item = mock(Item.class);
            User author = mock(User.class);

            Comment result = commentMapper.toComment(commentDto, item, author);

            assertEquals(commentDto.getId(), result.getId());
            assertEquals(commentDto.getText(), result.getText());
        }

        @Test
        public void shouldReturnNull() {
            Comment result = commentMapper.toComment(null, null, null);
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Маппинг в List<Comment>")
    class ToCommentList {
        @Test
        public void shouldReturnCommentList() {
            List<CommentDto> commentDtoList = new ArrayList<>();
            CommentDto commentDto1 = new CommentDto();
            commentDto1.setId(1L);
            commentDto1.setText("Comment 1");
            CommentDto commentDto2 = new CommentDto();
            commentDto2.setId(2L);
            commentDto2.setText("Comment 2");
            commentDtoList.add(commentDto1);
            commentDtoList.add(commentDto2);

            List<Comment> result = commentMapper.toCommentList(commentDtoList);

            assertEquals(commentDtoList.size(), result.size());
            for (int i = 0; i < commentDtoList.size(); i++) {
                assertEquals(commentDtoList.get(i).getId(), result.get(i).getId());
                assertEquals(commentDtoList.get(i).getText(), result.get(i).getText());
            }
        }

        @Test
        public void shouldReturnEmptyList() {
            List<Comment> result = commentMapper.toCommentList(new ArrayList<>());

            assertEquals(0, result.size());
        }
    }
}