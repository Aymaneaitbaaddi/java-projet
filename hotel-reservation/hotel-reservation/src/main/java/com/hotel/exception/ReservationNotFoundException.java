package com.hotel.exception;

/** Thrown when no reservation matches the given identifier. */
public class ReservationNotFoundException extends Exception {

    public ReservationNotFoundException(String message) {
        super(message);
    }
}
