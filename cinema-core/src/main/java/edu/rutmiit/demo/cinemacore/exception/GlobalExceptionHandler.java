package edu.rutmiit.demo.cinemacore.exception;

import edu.rutmiit.demo.cinemaapicontract.dto.ErrorResponse;
import edu.rutmiit.demo.cinemaapicontract.exception.BookingStateConflictException;
import edu.rutmiit.demo.cinemaapicontract.exception.CustomerAlreadyExistsException;
import edu.rutmiit.demo.cinemaapicontract.exception.ResourceNotFoundException;
import edu.rutmiit.demo.cinemaapicontract.exception.SeatAlreadyReservedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String BASE = "https://api.cinema.local/problems/";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "resource-not-found", "Ресурс не найден", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler({SeatAlreadyReservedException.class, BookingStateConflictException.class, CustomerAlreadyExistsException.class, ApiConflictException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "conflict", "Конфликт бизнес-правил", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = null;
        String detail = ex.getMessage();
        if (ex instanceof MethodArgumentNotValidException manv) {
            fieldErrors = manv.getBindingResult().getFieldErrors().stream()
                    .map(this::mapFieldError)
                    .toList();
            detail = "Ошибка валидации входных данных";
        }
        return build(HttpStatus.BAD_REQUEST, "validation-error", "Ошибка валидации", detail, request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "Внутренняя ошибка", ex.getMessage(), request.getRequestURI(), null);
    }

    private ErrorResponse.FieldError mapFieldError(FieldError error) {
        return new ErrorResponse.FieldError(error.getField(), error.getRejectedValue(), error.getDefaultMessage());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String type, String title, String detail, String uri, List<ErrorResponse.FieldError> fieldErrors) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                status.value(), BASE + type, title, detail, uri, Instant.now(), fieldErrors
        ));
    }
}
