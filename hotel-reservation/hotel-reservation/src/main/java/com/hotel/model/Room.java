package com.hotel.model;

/**
 * Common base of every hotel room.
 *
 * Abstract because a "generic" room does not exist in the hotel: every room is
 * a single room, a double room or a suite, and each type has its own pricing rule.
 */
public abstract class Room {

    /** Price of breakfast per guest and per night, for rooms where it is an option. */
    public static final double BREAKFAST_PRICE_PER_GUEST = 12.0;

    private final int number;
    private final double pricePerNight;
    private final int capacity;

    protected Room(int number, double pricePerNight, int capacity) {
        if (number <= 0) {
            throw new IllegalArgumentException("Room number must be positive: " + number);
        }
        if (pricePerNight <= 0) {
            throw new IllegalArgumentException("Price per night must be positive: " + pricePerNight);
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive: " + capacity);
        }
        this.number = number;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
    }

    /**
     * Calculates the price of a stay in this room, before any discount.
     * Each room type implements its own rule (polymorphism).
     */
    public abstract double calculatePrice(int nights, int guests, boolean breakfast);

    /** Human-readable name of the room type, used for display. */
    public abstract RoomType getType();

    public boolean canAccommodate(int guests) {
        return guests > 0 && guests <= capacity;
    }

    /** Shared helper for the room types where breakfast is a paid option. */
    protected double breakfastCost(int nights, int guests, boolean breakfast) {
        return breakfast ? BREAKFAST_PRICE_PER_GUEST * guests * nights : 0.0;
    }

    public int getNumber() {
        return number;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return String.format("Room %d (%s) - %.2f EUR/night - up to %d guest(s)",
                number, getType(), pricePerNight, capacity);
    }
}
