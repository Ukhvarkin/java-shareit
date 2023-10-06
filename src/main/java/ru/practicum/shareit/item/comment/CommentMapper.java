package ru.practicum.shareit.item.comment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CommentMapper {
    @Mapping(target = "authorName", source = "author.name")
    CommentDto toCommentDto(Comment comment);

    Comment toComment(CommentDto commentDto);
}
