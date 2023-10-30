package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler({UserNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(UserNotFoundException e) {
        String message = "Пользователь не найден.";
        log.error(message, e);
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({ItemNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleItemNotFoundException(ItemNotFoundException e) {
        String message = "Вещь не найдена.";
        log.error(message, e);
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({BookingNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleItemNotFoundException(BookingNotFoundException e) {
        String message = "Бронирование не найдено.";
        log.error(message, e);
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUserAlreadyExistsException(ValidationException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({ConflictException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUserConflictException(ConflictException e) {
        return new ErrorResponse(e.getMessage());
    }


}
