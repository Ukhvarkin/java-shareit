package ru.practicum.shareit.item.comment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "authorName", source = "author.name")
    CommentDto toCommentDto(Comment comment);

    @Mapping(target = "id", source = "commentDto.id")
    Comment toComment(CommentDto commentDto, Item item, User author);

    List<Comment> toCommentList(List<CommentDto> commentDtoList);
}
