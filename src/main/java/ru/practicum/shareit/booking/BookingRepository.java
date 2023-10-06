package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(Long userId, LocalDateTime start,
                                                                            BookingStatus status);

    List<Booking> findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(Long userId, LocalDateTime start,
                                                                          BookingStatus status);

    List<Booking> findByBookerIdOrderByStartDesc(Long booker);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId, LocalDateTime start,
                                                                          LocalDateTime end);

    List<Booking> findByBookerIdAndEndBeforeAndStatusEqualsOrderByStartDesc(Long userId, LocalDateTime start,
                                                                            BookingStatus status);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime start);

    List<Booking> findByBookerIdAndStatusEqualsOrderByStartDesc(Long userId, BookingStatus status);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long booker);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId, LocalDateTime start,
                                                                             LocalDateTime end);

    List<Booking> findByItemOwnerIdAndEndBeforeAndStatusEqualsOrderByStartDesc(Long userId, LocalDateTime start,
                                                                               BookingStatus status);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime start);

    List<Booking> findByItemOwnerIdAndStatusEqualsOrderByStartDesc(Long userId, BookingStatus status);

    @Query("select b from Booking b " +
        "where b.item.id = ?1 and " +
        "b.item.owner.id = ?2 and " +
        "b.end < ?3 order by b.start desc")
    List<Booking> findPastOwnerBookings(long itemId, long ownerId, LocalDateTime now);

    @Query("select b from Booking b " +
        "where b.item.id = ?1 and " +
        "b.item.owner.id = ?2 and " +
        "b.start > ?3 " +
        "order by b.start desc")
    List<Booking> findFutureOwnerBookings(long itemId, long ownerId, LocalDateTime now);

    Long countAllByItemIdAndBookerIdAndEndBefore(long itemId, long userId, LocalDateTime now);

    List<Booking> findByItemIdAndStartBeforeAndEndAfterAndStatusEqualsOrderByStartAsc(Long userId, LocalDateTime start,
                                                                                      LocalDateTime end,
                                                                                      BookingStatus status);

    List<Booking> findByItemIdAndStartAfterAndEndBeforeAndStatusEqualsOrderByStartAsc(Long id, LocalDateTime start,
                                                                                      LocalDateTime end,
                                                                                      BookingStatus status);

    List<Booking> findByItemId(Long id);


}
