package com.hotel.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationTest {

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        // Stay from the 10th to the 15th of December: 5 nights.
        reservation = new Reservation("R001", new Customer("Alice", "alice@mail.com"),
                new SingleRoom(101, 60.0, 1),
                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 15), 1, false, 300.0);
    }

    @Test
    void nightsAreCountedBetweenCheckInAndCheckOut() {
        assertEquals(5, reservation.getNights());
    }

    @Test
    void periodInsideTheStayOverlaps() {
        assertTrue(reservation.overlaps(LocalDate.of(2026, 12, 11), LocalDate.of(2026, 12, 12)));
    }

    @Test
    void periodStartingDuringTheStayOverlaps() {
        assertTrue(reservation.overlaps(LocalDate.of(2026, 12, 14), LocalDate.of(2026, 12, 20)));
    }

    @Test
    void checkInOnTheCheckOutDayDoesNotOverlap() {
        assertFalse(reservation.overlaps(LocalDate.of(2026, 12, 15), LocalDate.of(2026, 12, 18)));
    }

    @Test
    void checkOutOnTheCheckInDayDoesNotOverlap() {
        assertFalse(reservation.overlaps(LocalDate.of(2026, 12, 8), LocalDate.of(2026, 12, 10)));
    }

    @Test
    void newReservationIsConfirmed() {
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertTrue(reservation.isActive());
    }

    @Test
    void cancellingTwiceIsRefused() {
        reservation.cancel();

        assertThrows(IllegalStateException.class, () -> reservation.cancel());
    }
}
