package com.hotel.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomPricingTest {

    private static final double DELTA = 0.001;

    @Test
    void singleRoomPriceIsPricePerNightTimesNights() {
        Room room = new SingleRoom(101, 60.0, 1);

        double price = room.calculatePrice(3, 1, false);

        assertEquals(180.0, price, DELTA);
    }

    @Test
    void singleRoomChargesBreakfastPerGuestAndPerNight() {
        Room room = new SingleRoom(101, 60.0, 1);

        double price = room.calculatePrice(3, 1, true);

        assertEquals(180.0 + 3 * 12.0, price, DELTA);
    }

    @Test
    void doubleRoomWithOneGuestHasNoSupplement() {
        Room room = new DoubleRoom(103, 90.0, 2);

        double price = room.calculatePrice(2, 1, false);

        assertEquals(180.0, price, DELTA);
    }

    @Test
    void doubleRoomWithTwoGuestsAddsSupplementAndBreakfastForBoth() {
        Room room = new DoubleRoom(103, 90.0, 2);

        double price = room.calculatePrice(2, 2, true);

        // 2 nights x 90 + 2 nights x 15 supplement + 2 guests x 2 nights x 12 breakfast
        assertEquals(180.0 + 30.0 + 48.0, price, DELTA);
    }

    @Test
    void suiteAddsServiceFeeEveryNight() {
        Room room = new Suite(202, 180.0, 4);

        double price = room.calculatePrice(2, 2, false);

        assertEquals((180.0 + 30.0) * 2, price, DELTA);
    }

    @Test
    void suiteNeverChargesBreakfastBecauseItIsIncluded() {
        Room room = new Suite(202, 180.0, 4);

        double withBreakfast = room.calculatePrice(2, 4, true);
        double withoutBreakfast = room.calculatePrice(2, 4, false);

        assertEquals(withoutBreakfast, withBreakfast, DELTA);
    }

    @Test
    @DisplayName("The same call gives a different price depending on the actual room type")
    void priceDependsOnActualRoomType() {
        List<Room> rooms = List.of(
                new SingleRoom(1, 100.0, 1),
                new DoubleRoom(2, 100.0, 2),
                new Suite(3, 100.0, 2));

        List<Double> prices = rooms.stream()
                .map(room -> room.calculatePrice(1, 1, false))
                .toList();

        assertEquals(List.of(100.0, 100.0, 130.0), prices);
    }

    @Test
    void canAccommodateUpToCapacity() {
        Room room = new DoubleRoom(103, 90.0, 2);

        assertTrue(room.canAccommodate(2));
        assertFalse(room.canAccommodate(3));
        assertFalse(room.canAccommodate(0));
    }

    @Test
    void roomWithNegativePriceIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new SingleRoom(101, -10.0, 1));
    }

    @Test
    void roomWithZeroCapacityIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new Suite(202, 180.0, 0));
    }
}
