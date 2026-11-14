package com.hotel.loader;

import com.hotel.model.DoubleRoom;
import com.hotel.model.Room;
import com.hotel.model.RoomType;
import com.hotel.model.SingleRoom;
import com.hotel.model.Suite;

/**
 * Creates the right Room subclass from its type (Simple Factory pattern).
 * The rest of the application never needs to know which constructor to call.
 */
public final class RoomFactory {

    private RoomFactory() {
        // Utility class: no instance needed.
    }

    public static Room create(RoomType type, int number, double pricePerNight, int capacity) {
        return switch (type) {
            case SINGLE -> new SingleRoom(number, pricePerNight, capacity);
            case DOUBLE -> new DoubleRoom(number, pricePerNight, capacity);
            case SUITE -> new Suite(number, pricePerNight, capacity);
        };
    }
}
