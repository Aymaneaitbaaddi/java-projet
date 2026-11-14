package com.hotel.model;

/**
 * A premium room.
 * A fixed service fee is added to every night, and breakfast is always included:
 * the breakfast option is therefore never charged, whatever the guest chooses.
 */
public class Suite extends Room {

    public static final double SERVICE_FEE_PER_NIGHT = 30.0;

    public Suite(int number, double pricePerNight, int capacity) {
        super(number, pricePerNight, capacity);
    }

    @Override
    public double calculatePrice(int nights, int guests, boolean breakfast) {
        return (getPricePerNight() + SERVICE_FEE_PER_NIGHT) * nights;
    }

    @Override
    public RoomType getType() {
        return RoomType.SUITE;
    }
}
