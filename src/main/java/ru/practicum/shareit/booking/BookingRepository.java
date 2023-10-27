package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Booking> findByBookerId(Long booker, Pageable pageable);

    Page<Booking> findByBookerIdAndStartBeforeAndEndAfter(Long userId, LocalDateTime start,
                                                          LocalDateTime end, Pageable pageable);

    Page<Booking> findByBookerIdAndEndBeforeAndStatusEquals(Long userId, LocalDateTime start,
                                                            BookingStatus status, Pageable pageable);

    Page<Booking> findByBookerIdAndStartAfter(Long userId, LocalDateTime start, Pageable pageable);

    Page<Booking> findByBookerIdAndStatusEquals(Long userId, BookingStatus status, Pageable pageable);

    Page<Booking> findByItemOwnerId(Long booker, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(Long userId, LocalDateTime start,
                                                             LocalDateTime end, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndEndBeforeAndStatusEquals(Long userId, LocalDateTime start,
                                                               BookingStatus status, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndStartAfter(Long userId, LocalDateTime start, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndStatusEquals(Long userId, BookingStatus status,
                                                   Pageable pageable);

    Long countAllByItemIdAndBookerIdAndEndBefore(long itemId, long userId, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
        "WHERE b.item.id = ?1 " +
        "AND b.end > ?2 " +
        "AND b.start < ?3 " +
        "AND b.status = ?4 " +
        "ORDER BY b.start ASC")
    List<Booking> findBookingsByItemIdAndTimeRangeAndStatus(Long userId, LocalDateTime start,
                                                            LocalDateTime end,
                                                            BookingStatus status);

    @Query("SELECT b FROM Booking b " +
        "WHERE b.item.id = ?1 " +
        "AND b.start > ?2 " +
        "AND b.end < ?3 " +
        "AND b.status = ?4 " +
        "ORDER BY b.start ASC")
    List<Booking> findByItemIdAndTimeRangeAndStatus(Long id, LocalDateTime start,
                                                    LocalDateTime end,
                                                    BookingStatus status);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
        "WHERE b.item.id = ?1 " +
        "AND ((b.start < ?3 AND b.end > ?2) OR (b.start <= ?2 AND b.end >= ?3))")
    boolean hasOverlappingBookings1(Long itemId, LocalDateTime start, LocalDateTime end);
}
