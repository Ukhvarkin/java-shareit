package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addItemRequest(ItemRequestDto itemRequestDto, Long userId);

    List<ItemRequestResponseDto> findAllItemRequestByRequestorId(Long requestorId);

    List<ItemRequestResponseDto> findAllItemRequests(Long userId, Pageable pageable);

    ItemRequestResponseDto findItemRequestById(Long userId, Long requestId);
}
