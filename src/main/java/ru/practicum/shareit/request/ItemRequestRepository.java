package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    Page<ItemRequest> findByRequestorId_IdNot(Long userId, Pageable pageable);

    List<ItemRequest> findByRequestorId_IdOrderByCreatedDesc(Long requestorId);
}