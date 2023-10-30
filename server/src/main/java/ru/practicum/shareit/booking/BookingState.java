package ru.practicum.shareit.booking;

import ru.practicum.shareit.exception.ValidationException;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    static BookingState stateValid(String x) throws RuntimeException {
        for (BookingState currentType : BookingState.values()) {
            if (x.equals(currentType.toString())) {
                return currentType;
            }
        }
        throw new ValidationException("Unknown state: " + x);
    }
}
