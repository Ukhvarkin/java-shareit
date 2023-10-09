package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    public ItemDto addItem(ItemDto itemDto, Long userId) {
        itemDtoValidation(itemDto);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Пользователь с id: " + userId));
        List<Comment> comments = new ArrayList<>();
        Item item = itemMapper.toItem(itemDto, user, comments);
        log.info("Добавлена вещь: {}, пользователя c id: {}.", item, item.getOwner().getId());
        return itemMapper.toItemDto(itemRepository.save(item), null, null, new ArrayList<>());
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long userId) {
        Item existingItem = itemRepository.findByIdAndOwner_Id(itemDto.getId(), userId)
            .orElseThrow(() -> new ItemNotFoundException("Вещь с id: " + itemDto.getId() +
                " не найдена для пользователя с id: " + userId));

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
            log.info("Редактирование имени: {}", itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
            log.info("Редактирование описания: {}", itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
            log.info("Редактирование наличия: {}", itemDto.getAvailable());
        }
        itemRepository.save(existingItem);
        BookingItemDto lastBooking = bookingLast(existingItem);
        BookingItemDto nextBooking = bookingNext(existingItem);
        List<CommentDto> comments = getCommentForItem(existingItem);
        return itemMapper.toItemDto(existingItem, lastBooking, nextBooking, comments);
    }


    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        Item existingItem = itemRepository.findById(itemId)
            .orElseThrow(() -> new ItemNotFoundException("Вещь с id: " + itemId));

        if (!Objects.equals(userId, existingItem.getOwner().getId())) {
            return itemMapper.toItemDto(existingItem, null, null, getCommentForItem(existingItem));
        } else {
            return itemMapper.toItemDto(existingItem, bookingLast(existingItem),
                bookingNext(existingItem), getCommentForItem(existingItem));
        }
    }

    @Override
    public List<ItemDto> findAllItemsByOwner(Long userId, int from, int size) {
        Pageable page = PageRequest.of(from, size);

        userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Пользователь с id: " + userId));

        List<Item> items = itemRepository.findAllByOwnerId(userId, page);
        return getItemDto(items);
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        List<Item> result = itemRepository.search(text);
        return getItemDto(result);
    }

    private List<ItemDto> getItemDto(List<Item> items) {
        List<ItemDto> list = new ArrayList<>();
        for (Item item : items) {
            BookingItemDto lastBooking = bookingLast(item);

            BookingItemDto nextBooking = bookingNext(item);

            List<CommentDto> commentsDto = getCommentForItem(item);
            item.setComments(commentMapper.toCommentList(commentsDto));
            list.add(itemMapper.toItemDto(item, lastBooking, nextBooking, commentsDto));
        }
        return list;
    }

    @Override
    public CommentDto addComment(Long itemId, CommentDto commentDto, Long userId) {
        commentDtoValidation(commentDto);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("Пользователь с id: " + userId));

        Long bookingsCount =
            bookingRepository.countAllByItemIdAndBookerIdAndEndBefore(itemId, userId, LocalDateTime.now());

        if (bookingsCount == null || bookingsCount == 0) {
            throw new ValidationException("Вы не брали эту вещь в аренду.");
        }
        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new UserNotFoundException("Вещь с id: " + itemId));

        Comment comment = commentMapper.toComment(commentDto, item, user);
        comment.setCreated(LocalDateTime.now());
        log.info("Добавлен новый комментарий к вещи с id: {}.", itemId);
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    private BookingItemDto bookingLast(Item item) {
        List<Booking> bookings = bookingRepository.findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(
            item.getId(), LocalDateTime.now(), BookingStatus.APPROVED);

        if (bookings.isEmpty()) {
            return null;
        } else {
            Booking lastBooking = bookings.get(0);
            return itemMapper.bookingToBookingItemDto(lastBooking);
        }
    }

    private BookingItemDto bookingNext(Item item) {
        List<Booking> bookings = bookingRepository.findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(
            item.getId(), LocalDateTime.now(), BookingStatus.APPROVED);

        if (bookings.isEmpty()) {
            return null;
        } else {
            Booking nextBooking = bookings.get(0);
            return itemMapper.bookingToBookingItemDto(nextBooking);
        }
    }

    private List<CommentDto> getCommentForItem(Item item) {
        return commentRepository.getAllByItemIdOrderByCreatedAsc(item.getId())
            .stream()
            .map(commentMapper::toCommentDto)
            .collect(Collectors.toList());
    }

    private void itemDtoValidation(ItemDto itemDto) {
        if (itemDto == null) {
            String message = "Некорректный ввод. Пустой объект.";
            log.warn(message);
            throw new ValidationException(message);
        }
        if (itemDto.getName() == null) {
            String message = "Некорректный ввод, пустое поле имени.";
            log.warn(message);
            throw new ValidationException(message);
        }
        if (itemDto.getName().isEmpty()) {
            String message = "Некорректный ввод, пустое поле имени.";
            log.warn(message);
            throw new ValidationException(message);
        }
        if (itemDto.getDescription() == null) {
            String message = "Некорректный ввод, пустое поле описания.";
            log.warn(message);
            throw new ValidationException(message);
        }
        if (itemDto.getAvailable() == null) {
            String message = "Некорректный ввод, пустое поле наличия вещи.";
            log.warn(message);
            throw new ValidationException(message);
        }
    }

    private void commentDtoValidation(CommentDto commentDto) {
        if (commentDto.getText() == null) {
            String message = "Некорректный ввод. Пустой текст.";
            log.warn(message);
            throw new ValidationException(message);
        }

        if (commentDto.getText().equals(" ") || commentDto.getText().isEmpty()) {
            String message = "Некорректный ввод. Пустой текст.";
            log.warn(message);
            throw new ValidationException(message);
        }
    }

}
