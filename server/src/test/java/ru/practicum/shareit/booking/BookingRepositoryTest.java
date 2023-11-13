package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingRepositoryTest {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    private final int from = 0;
    private final int size = 10;
    private final Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Order.desc("start")));
    private final LocalDateTime localDateTime = LocalDateTime.of(2000, 1, 1, 1, 1, 1);
    private final User user1 = User.builder()
        .id(1L)
        .name("One")
        .email("one@yandex.ru")
        .build();
    private final User user2 = User.builder()
        .id(2L)
        .name("Two")
        .email("two@yandex.ru")
        .build();
    private final Item item1 = Item.builder()
        .id(1L)
        .name("item")
        .description("description")
        .available(true)
        .owner(user1)
        .build();
    private final Booking bookingPast = Booking.builder()
        .id(1L)
        .start(localDateTime.minusYears(10))
        .end(localDateTime.minusYears(9))
        .item(item1)
        .booker(user2)
        .status(BookingStatus.APPROVED)
        .build();
    private final Booking bookingCurrent = Booking.builder()
        .id(2L)
        .start(localDateTime.minusYears(5))
        .end(localDateTime.plusYears(5))
        .item(item1)
        .booker(user2)
        .status(BookingStatus.APPROVED)
        .build();
    private final Booking bookingFuture = Booking.builder()
        .id(3L)
        .start(localDateTime.plusYears(8))
        .end(localDateTime.plusYears(9))
        .item(item1)
        .booker(user2)
        .status(BookingStatus.WAITING)
        .build();
    private final Booking bookingRejected = Booking.builder()
        .id(4L)
        .start(localDateTime.plusYears(9))
        .end(localDateTime.plusYears(10))
        .item(item1)
        .booker(user2)
        .status(BookingStatus.REJECTED)
        .build();

    @BeforeEach
    public void beforeEach() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        bookingRepository.save(bookingPast);
        bookingRepository.save(bookingCurrent);
        bookingRepository.save(bookingFuture);
        bookingRepository.save(bookingRejected);
    }

    @Nested
    @DisplayName("Получить вещь по id, с датой начала до указанной, status: APPROVED, сортировка: Desc")
    class FindByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc {
        @Test
        @DisplayName("Положительный тест")
        public void shouldGetLastBookingsTest() {
            List<Booking> result = bookingRepository.findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(
                item1.getId(),
                localDateTime,
                BookingStatus.APPROVED);

            assertEquals(2, result.size());
            assertEquals(bookingCurrent.getId(), result.get(0).getId());
            assertEquals(bookingPast.getId(), result.get(1).getId());
        }

        @Test
        @DisplayName("Негативный тест")
        public void shouldGetEmptyTest() {
            List<Booking> result = bookingRepository.findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(
                item1.getId(), localDateTime.minusYears(15), BookingStatus.APPROVED);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Получить вещь по id, с датой начала после указанной, status: WAITING, сортировка: Asc")
    class FindByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc {
        @Test
        @DisplayName("Положительный тест")
        public void shouldGetNextBookingsTest() {
            List<Booking> result = bookingRepository.findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(
                item1.getId(),
                localDateTime,
                BookingStatus.WAITING);

            assertEquals(1, result.size());
            assertEquals(bookingFuture.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Негативный тест")
        public void shouldGetEmptyTest() {
            List<Booking> result = bookingRepository.findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(
                item1.getId(),
                localDateTime,
                BookingStatus.APPROVED);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Получить вещь по booker id, сортировка: Desc")
    class FindByBookerIdOrderByStartDesc {
        @Test
        @DisplayName("Положительный тест")
        public void shouldGetAllTest() {
            List<Booking> result = bookingRepository
                .findByBookerId(user2.getId(), pageable)
                .get().collect(Collectors.toList());

            assertEquals(4, result.size());
            assertEquals(bookingRejected.getId(), result.get(0).getId());
            assertEquals(bookingFuture.getId(), result.get(1).getId());
            assertEquals(bookingCurrent.getId(), result.get(2).getId());
            assertEquals(bookingPast.getId(), result.get(3).getId());
        }

        @Test
        @DisplayName("Негативный тест")
        public void shouldGetEmptyTest() {
            List<Booking> result = bookingRepository.findByBookerId(user1.getId(), pageable)
                .get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Получить вещь по booker id, " +
        "с датой начала перед указанной и датой окончания после указанной, " +
        "сортировка: Asc")
    class FindByBookerIdAndStartBeforeAndEndAfterOrderByStartAsc {
        @Test
        @DisplayName("Положительный тест")
        public void shouldGetCurrentTest() {
            List<Booking> result = bookingRepository
                .findByBookerIdAndStartBeforeAndEndAfter(
                    user2.getId(),
                    localDateTime,
                    localDateTime,
                    pageable).get().collect(Collectors.toList());

            assertEquals(1, result.size());
            assertEquals(bookingCurrent.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Негативный тест")
        public void shouldGetEmptyTest() {
            List<Booking> result = bookingRepository
                .findByBookerIdAndStartBeforeAndEndAfter(user1.getId(), localDateTime,
                    localDateTime, pageable).get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Получить вещь по booker id, с окончанием до указанной даты, status: APPROVED, сортировка: Desc")
    class FindByBookerIdAndEndBeforeAndStatusEqualsOrderByStartDesc {
        @Test
        @DisplayName("Положительный тест")
        public void shouldGetPastTest() {
            List<Booking> result = bookingRepository
                .findByBookerIdAndEndBeforeAndStatusEquals(
                    user2.getId(),
                    localDateTime,
                    BookingStatus.APPROVED,
                    pageable).get().collect(Collectors.toList());

            assertEquals(1, result.size());
            assertEquals(bookingPast.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Негативный тест")
        public void shouldGetEmptyTest() {
            List<Booking> result = bookingRepository
                .findByBookerIdAndEndBeforeAndStatusEquals(
                    user1.getId(),
                    localDateTime,
                    BookingStatus.APPROVED,
                    pageable).get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Получить вещь по booker id, с началом после указанной даты, сортировка: Desc")
    class FindByBookerIdAndStartAfterOrderByStartDesc {
        @Test
        @DisplayName("Положительный тест")
        public void shouldGetFutureTest() {
            List<Booking> result = bookingRepository
                .findByBookerIdAndStartAfter(
                    user2.getId(),
                    localDateTime,
                    pageable)
                .get().collect(Collectors.toList());

            assertEquals(2, result.size());
            assertEquals(bookingRejected.getId(), result.get(0).getId());
            assertEquals(bookingFuture.getId(), result.get(1).getId());
        }

        @Test
        @DisplayName("Негативный тест")
        public void shouldGetEmptyTest() {
            List<Booking> result = bookingRepository
                .findByBookerIdAndStartAfter(
                    user1.getId(),
                    localDateTime,
                    pageable)
                .get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Получить вещь по booker id, status: WAITING, сортировка: Desc")
    class FindByBookerIdAndStatusEqualsOrderByStartDesc {
        @Test
        @DisplayName("Положительный тест")
        public void shouldGetWaitingTest() {
            List<Booking> result = bookingRepository
                .findByBookerIdAndStatusEquals(
                    user2.getId(),
                    BookingStatus.WAITING,
                    pageable)
                .get().collect(Collectors.toList());

            assertEquals(1, result.size());
            assertEquals(bookingFuture.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Положительный тест")
        public void shouldGetRejectedTest() {
            List<Booking> result = bookingRepository
                .findByBookerIdAndStatusEquals(
                    user2.getId(),
                    BookingStatus.REJECTED,
                    pageable)
                .get().collect(Collectors.toList());

            assertEquals(1, result.size());
            assertEquals(bookingRejected.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Негативный тест")
        public void shouldGetEmptyTest() {
            List<Booking> result = bookingRepository
                .findByBookerIdAndStatusEquals(user1.getId(), BookingStatus.WAITING, pageable)
                .get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Получить вещь по owner id, сортировка: Desc")
    class FindByItemOwnerIdOrderByStartDesc {
        @Test
        @DisplayName("Положительный тест")
        public void shouldGetAllTest() {
            List<Booking> result = bookingRepository.findByItemOwnerId(
                    user1.getId(),
                    pageable)
                .get().collect(Collectors.toList());

            assertEquals(4, result.size());
            assertEquals(bookingRejected.getId(), result.get(0).getId());
            assertEquals(bookingFuture.getId(), result.get(1).getId());
            assertEquals(bookingCurrent.getId(), result.get(2).getId());
            assertEquals(bookingPast.getId(), result.get(3).getId());
        }

        @Test
        @DisplayName("Негативный тест")
        public void shouldGetEmptyTest() {
            List<Booking> result = bookingRepository.findByItemOwnerId(user2.getId(), pageable)
                .get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Получить вещь по owner id, " +
        "с датой начала перед указанной и датой окончания после указанной, " +
        "сортировка: Desc")
    class FindByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc {
        @Test
        @DisplayName("Положительный тест")
        public void shouldGetCurrentTest() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(
                user1.getId(),
                localDateTime,
                localDateTime,
                pageable).get().collect(Collectors.toList());

            assertEquals(1, result.size());
            assertEquals(bookingCurrent.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Негативный тест")
        public void shouldGetEmptyTest() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(
                user2.getId(), localDateTime, localDateTime, pageable).get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Получить вещь по owner id, с окончанием до указанной даты, status: APPROVED, сортировка: Desc")
    class FindByItemOwnerIdAndEndBeforeAndStatusEqualsOrderByStartDesc {
        @Test
        @DisplayName("Положительный тест")
        public void shouldGetPastTest() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndEndBeforeAndStatusEquals(
                user1.getId(),
                localDateTime,
                BookingStatus.APPROVED,
                pageable).get().collect(Collectors.toList());

            assertEquals(1, result.size());
            assertEquals(bookingPast.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Негативный тест")
        public void shouldGetEmptyTest() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndEndBeforeAndStatusEquals(
                user2.getId(), localDateTime, BookingStatus.APPROVED, pageable).get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Получить вещь по owner id, с началом после указанной даты, сортировка: Desc")
    class FindByItemOwnerIdAndStartAfterOrderByStartDesc {
        @Test
        @DisplayName("Положительный тест")
        public void shouldGetFutureTest() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndStartAfter(user1.getId(),
                localDateTime, pageable).get().collect(Collectors.toList());

            assertEquals(2, result.size());
            assertEquals(bookingRejected.getId(), result.get(0).getId());
            assertEquals(bookingFuture.getId(), result.get(1).getId());
        }

        @Test
        @DisplayName("Негативный тест")
        public void shouldGetEmptyTest() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndStartAfter(user2.getId(),
                localDateTime, pageable).get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Получить вещь по owner id, status: WAITING, сортировка: Desc")
    class FindByItemOwnerIdAndStatusEqualsOrderByStartDesc {
        @Test
        @DisplayName("Положительный тест")
        public void shouldGetWaitingTest() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndStatusEquals(
                user1.getId(),
                BookingStatus.WAITING,
                pageable).get().collect(Collectors.toList());

            assertEquals(1, result.size());
            assertEquals(bookingFuture.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Положительный тест")
        public void shouldGetRejectedTest() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndStatusEquals(user1.getId(),
                BookingStatus.REJECTED, pageable).get().collect(Collectors.toList());

            assertEquals(1, result.size());
            assertEquals(bookingRejected.getId(), result.get(0).getId());
        }

        @Test
        @DisplayName("Негативный тест")
        public void shouldGetEmptyTest() {
            List<Booking> result = bookingRepository.findByItemOwnerIdAndStatusEquals(user2.getId(),
                BookingStatus.WAITING, pageable).get().collect(Collectors.toList());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Подсчет бронирований по id вещи, booker id, дата окончания")
    class CountAllByItemIdAndBookerIdAndEndBefore {
        @Test
        @DisplayName("Положительный тест")
        public void shouldCountBookingsBeforeEndTest() {
            Long count = bookingRepository.countAllByItemIdAndBookerIdAndEndBefore(
                item1.getId(), user2.getId(), localDateTime.minusYears(8)
            );

            assertEquals(1, count);
        }

        @Test
        @DisplayName("Негативный тест")
        public void shouldCountNoBookingsTest() {
            Long count = bookingRepository.countAllByItemIdAndBookerIdAndEndBefore(
                item1.getId(), user2.getId(), localDateTime.minusYears(12)
            );

            assertEquals(0, count);
        }

    }

    @Nested
    @DisplayName("Получить бронирование по id вещи, временному диапазону, status: APPROVED")
    class FindBookingsByItemIdAndTimeRangeAndStatus {
        @Test
        @DisplayName("Положительный тест")
        public void shouldFindBookingsInTimeRangeAndStatusTest() {
            List<Booking> result = bookingRepository.findBookingsByItemIdAndTimeRangeAndStatus(
                item1.getId(),
                localDateTime.minusYears(6),
                localDateTime.plusYears(6),
                BookingStatus.APPROVED
            );

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Негативный тест")
        public void shouldFindNoBookingsTest() {
            List<Booking> result = bookingRepository.findBookingsByItemIdAndTimeRangeAndStatus(
                item1.getId(), localDateTime.minusYears(12), localDateTime.minusYears(10), BookingStatus.APPROVED
            );

            assertTrue(result.isEmpty());
        }

    }

    @Nested
    @DisplayName("Получить бронирование по id вещи, временному диапазону, status: APPROVED")
    class FindByItemIdAndTimeRangeAndStatus {
        @Test
        @DisplayName("Положительный тест")
        public void shouldFindByItemIdAndTimeRangeAndStatus() {
            List<Booking> result = bookingRepository.findByItemIdAndTimeRangeAndStatus(
                item1.getId(),
                localDateTime.minusYears(6),
                localDateTime.plusYears(6),
                BookingStatus.APPROVED
            );

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Негативный тест")
        public void shouldFindNoBookingsWithFindByTest() {
            List<Booking> result = bookingRepository.findByItemIdAndTimeRangeAndStatus(
                item1.getId(), localDateTime.minusYears(12), localDateTime.minusYears(10), BookingStatus.APPROVED
            );

            assertTrue(result.isEmpty());
        }

    }

    @Nested
    @DisplayName("Проверка перекрывающихся бронирований")
    class HasOverlappingBookings {
        @Test
        @DisplayName("Положительный тест")
        public void shouldHaveOverlappingBookingsTest() {
            boolean hasOverlapping = bookingRepository.hasOverlappingBookings1(
                item1.getId(), localDateTime.minusYears(7), localDateTime.plusYears(7)
            );

            assertTrue(hasOverlapping);
        }

        @Test
        @DisplayName("Негативный тест")
        public void shouldNotHaveOverlappingBookingsTest() {
            boolean hasOverlapping = bookingRepository.hasOverlappingBookings1(
                item1.getId(), localDateTime.minusYears(12), localDateTime.minusYears(10)
            );

            assertFalse(hasOverlapping);
        }

    }
}