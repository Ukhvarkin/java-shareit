package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item as i " +
        "WHERE i.available = true AND " +
        "(LOWER(i.name) LIKE LOWER(CONCAT('%', ?1, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', ?1, '%')))")
    Page<Item> search(String text, Pageable pageable);

    List<Item> findAllByOwnerId(Long ownerId);

    Page<Item> findAllByOwnerId(Long ownerId, Pageable page);

    Optional<Item> findByIdAndOwner_Id(Long id, Long userId);

    List<Item> findByRequestId(Long requestId);

    List<Item> findByRequestIdIn(List<Long> ids);
}
