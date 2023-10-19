package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;


    @Override
    @Transactional
    public ItemRequestDto addItemRequest(ItemRequestDto itemRequestDto, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Не найден пользователь с id: " + userId));
        log.info("Найден пользователь с id: {}", user.getId());
        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestDto, user, LocalDateTime.now());
        return itemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestResponseDto> findAllItemRequestByRequestorId(Long requestorId) {
        User user = userRepository.findById(requestorId)
            .orElseThrow(() -> new UserNotFoundException("Не найден пользователь с id: " + requestorId));
        log.info("Найден пользователь с id: {}", user.getId());

        List<ItemRequest> itemRequests = itemRequestRepository.findByRequestorId_IdOrderByCreatedAsc(requestorId);
        log.info("найдено : {} запросов пользователя", itemRequests.size());


        List<Long> itemRequestIds = itemRequests.stream()
            .map(ItemRequest::getId)
            .collect(Collectors.toList());

        Map<Long, List<ItemDto>> itemDtoForRequestId = itemRepository.findByRequestIdIn(itemRequestIds)
            .stream()
            .map(itemMapper::toItemDto)
            .collect(Collectors.groupingBy(ItemDto::getRequestId));

        List<ItemRequestResponseDto> result = itemRequests.stream()
            .filter(itemRequest -> itemRequest.getRequestorId().getId().equals(requestorId))
            .map((itemRequest) -> itemRequestMapper.toItemRequestResponseDto(
                itemRequest,
                null)
            )
            .collect(Collectors.toList());

        for (ItemRequestResponseDto itemRequestExtendedDto : result) {
            if (itemDtoForRequestId.get(itemRequestExtendedDto.getId()) != null &&
                !itemDtoForRequestId.get(itemRequestExtendedDto.getId()).isEmpty()) {
                itemRequestExtendedDto.setItems(itemDtoForRequestId.get(itemRequestExtendedDto.getId()));
            } else {
                itemRequestExtendedDto.setItems(new ArrayList<>());
            }
        }
        return result;
    }

    @Override
    public List<ItemRequestResponseDto> findAllItemRequests(Long userId, Pageable pageable) {
        userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Не найден пользователь с id: " + userId));

        Page<ItemRequest> itemRequests = itemRequestRepository.findByRequestorId_IdNot(userId, pageable);
        List<Long> itemRequestIds = itemRequests.stream()
            .map(ItemRequest::getId)
            .collect(Collectors.toList());

        Map<Long, List<ItemDto>> itemDtoForRequestId = itemRepository.findByRequestIdIn(itemRequestIds)
            .stream()
            .map(itemMapper::toItemDto)
            .collect(Collectors.groupingBy(ItemDto::getRequestId));


        return itemRequests.stream()
            .map((itemRequest) -> itemRequestMapper.toItemRequestResponseDto(
                itemRequest,
                itemDtoForRequestId.get(itemRequest.getId())))
            .collect(Collectors.toList());
    }

    @Override
    public ItemRequestResponseDto findItemRequestById(Long userId, Long requestId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Не найден пользователь с id: " + userId));

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
            .orElseThrow(() -> new UserNotFoundException("Не найден запрос с id: " + requestId));

        List<ItemDto> items = itemRepository.findByRequestId(itemRequest.getId())
            .stream().map(itemMapper::toItemDto)
            .collect(Collectors.toList());

        return itemRequestMapper.toItemRequestResponseDto(itemRequest, items);
    }
}