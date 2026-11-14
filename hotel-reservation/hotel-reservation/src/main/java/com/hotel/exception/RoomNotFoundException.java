package com.hotel.exception;

/** Thrown when the requested room number does not exist in the hotel. */
public class RoomNotFoundException extends Exception {

    public RoomNotFoundException(String message) {
        super(message);
    }
}
