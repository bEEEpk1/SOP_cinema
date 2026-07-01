package edu.rutmiit.demo.cinemaapicontract.exception;

public class BookingStateConflictException extends RuntimeException {
    public BookingStateConflictException(String message) {
        super(message);
    }
}
