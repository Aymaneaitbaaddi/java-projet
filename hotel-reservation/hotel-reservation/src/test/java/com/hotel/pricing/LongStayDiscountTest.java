package com.hotel.pricing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LongStayDiscountTest {

    private static final double DELTA = 0.001;
    private final DiscountPolicy discount = new LongStayDiscount(7, 0.10);

    @Test
    void shortStayIsNotDiscounted() {
        assertEquals(600.0, discount.apply(600.0, 6), DELTA);
    }

    @Test
    void discountAppliesFromExactlyTheMinimumNights() {
        assertEquals(630.0, discount.apply(700.0, 7), DELTA);
    }

    @Test
    void longerStayIsDiscounted() {
        assertEquals(900.0, discount.apply(1000.0, 10), DELTA);
    }

    @Test
    void noDiscountLeavesPriceUnchanged() {
        assertEquals(700.0, new NoDiscount().apply(700.0, 30), DELTA);
    }

    @Test
    void rateAboveOneIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new LongStayDiscount(7, 1.5));
    }

    @Test
    void zeroMinimumNightsIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new LongStayDiscount(0, 0.10));
    }
}
