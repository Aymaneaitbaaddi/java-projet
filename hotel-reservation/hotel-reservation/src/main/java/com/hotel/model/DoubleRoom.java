package com.hotel.model;

/**
 * A room for up to two people.
 * The base price covers one guest; a second guest adds a supplement per night.
 * Breakfast is optional.
 */
public class DoubleRoom extends Room {

    public static final double SECOND_GUEST_SUPPLEMENT = 15.0;

    public DoubleRoom(int number, double pricePerNight, int capacity) {
        super(number, pricePerNight, capacity);
    }

    @Override
    public double calculatePrice(int nights, int guests, boolean breakfast) {
        double price = getPricePerNight() * nights;
        if (guests >= 2) {
            price += SECOND_GUEST_SUPPLEMENT * nights;
        }
        return price + breakfastCost(nights, guests, breakfast);
    }

    @Override
    public RoomType getType() {
        return RoomType.DOUBLE;
    }
}
