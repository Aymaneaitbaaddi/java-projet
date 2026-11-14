package com.hotel.exception;

/**
 * Thrown when a reservation request breaks a basic rule: invalid dates,
 * number of guests above the room capacity, or a cancellation that is not allowed.
 *
 * Unchecked exception (extends RuntimeException): the request itself is invalid
 * and should be rejected immediately (fail fast), like an IllegalArgumentException.
 */
public class InvalidReservationException extends RuntimeException {

    public InvalidReservationException(String message) {
        super(message);
    }
}
