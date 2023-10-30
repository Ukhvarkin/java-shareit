package ru.practicum.shareit.item.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c " +
        "WHERE c.item.id = ?1 " +
        "ORDER BY c.created ASC")
    List<Comment> getAllByItemIdOrderByCreatedAsc(Long id);
}
