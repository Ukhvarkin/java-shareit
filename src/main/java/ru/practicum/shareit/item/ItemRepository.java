package ru.practicum.shareit.item;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item as i " +
        "WHERE i.available = true AND " +
        "(LOWER(i.name) LIKE LOWER(CONCAT('%', ?1, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', ?1, '%')))")
    List<Item> search(String text);

    List<Item> findAllByOwnerId(Long ownerId);

    List<Item> findAllByOwnerId(Long ownerId, Pageable page);
}
