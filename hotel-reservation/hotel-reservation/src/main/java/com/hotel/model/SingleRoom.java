package com.hotel.model;

/** A room for one person. Price = price per night x nights, breakfast optional. */
public class SingleRoom extends Room {

    public SingleRoom(int number, double pricePerNight, int capacity) {
        super(number, pricePerNight, capacity);
    }

    @Override
    public double calculatePrice(int nights, int guests, boolean breakfast) {
        return getPricePerNight() * nights + breakfastCost(nights, guests, breakfast);
    }

    @Override
    public RoomType getType() {
        return RoomType.SINGLE;
    }
}
