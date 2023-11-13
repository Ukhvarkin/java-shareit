package ru.practicum.shareit.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {
    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "requestor", source = "user")
    @Mapping(target = "created", source = "now")
    ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User user, LocalDateTime now);

    ItemRequestDto toItemRequestDto(ItemRequest itemRequest);

    @Mapping(target = "items", source = "items")
    ItemRequestResponseDto toItemRequestResponseDto(ItemRequest itemRequest, List<ItemDto> items);
}
