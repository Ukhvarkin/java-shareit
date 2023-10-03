package ru.practicum.shareit.enums;

import lombok.Getter;

@Getter
public enum Status {
    WAITING("Новое бронирование, ожидает одобрения."),
    APPROVED("Бронирование подтверждено владельцем."),
    REJECTED("Бронирование отклонено владельцем."),
    CANCELED("Бронирование отменено создателем.");

    private final String title;

    Status(String title) {
        this.title = title;
    }

}
