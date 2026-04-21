package com.ticketbooking.exception;

public class SeatLockException extends RuntimeException {

    public SeatLockException(String message) {
        super(message);
    }
}
