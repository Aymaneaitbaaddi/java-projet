package com.hotel.exception;

/**
 * Thrown when a room is already booked for (part of) the requested period.
 *
 * Checked exception: this is an expected business situation that the caller
 * must handle, typically by proposing another room or other dates.
 */
public class RoomNotAvailableException extends Exception {

    public RoomNotAvailableException(String message) {
        super(message);
    }
}
