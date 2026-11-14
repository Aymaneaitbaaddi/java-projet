package com.hotel.service;

import com.hotel.exception.InvalidReservationException;
import com.hotel.exception.ReservationNotFoundException;
import com.hotel.exception.RoomNotAvailableException;
import com.hotel.exception.RoomNotFoundException;
import com.hotel.model.Customer;
import com.hotel.model.DoubleRoom;
import com.hotel.model.Reservation;
import com.hotel.model.ReservationStatus;
import com.hotel.model.Room;
import com.hotel.model.SingleRoom;
import com.hotel.model.Suite;
import com.hotel.pricing.LongStayDiscount;
import com.hotel.repository.InMemoryReservationRepository;
import com.hotel.repository.InMemoryRoomRepository;
import com.hotel.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationServiceTest {

    private static final double DELTA = 0.001;
    private static final LocalDate DEC_10 = LocalDate.of(2026, 12, 10);
    private static final LocalDate DEC_12 = LocalDate.of(2026, 12, 12);
    private static final LocalDate DEC_15 = LocalDate.of(2026, 12, 15);

    private ReservationService service;
    private Customer alice;
    private Customer bob;

    @BeforeEach
    void setUp() {
        RoomRepository rooms = new InMemoryRoomRepository();
        rooms.save(new SingleRoom(101, 60.0, 1));
        rooms.save(new DoubleRoom(103, 90.0, 2));
        rooms.save(new Suite(202, 180.0, 4));

        PricingService pricing = new PricingService(new LongStayDiscount(7, 0.10));
        service = new ReservationService(rooms, new InMemoryReservationRepository(), pricing);

        alice = new Customer("Alice", "alice@mail.com");
        bob = new Customer("Bob", "bob@mail.com");
    }

    // ----- Normal cases -----

    @Test
    void bookingAFreeRoomCreatesAConfirmedReservation() throws Exception {
        Reservation reservation = service.book(alice, 101, DEC_10, DEC_12, 1, false);

        assertNotNull(reservation.getId());
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertEquals(120.0, reservation.getTotalPrice(), DELTA);
    }

    @Test
    void reservationIdsAreUnique() throws Exception {
        Reservation first = service.book(alice, 101, DEC_10, DEC_12, 1, false);
        Reservation second = service.book(bob, 103, DEC_10, DEC_12, 1, false);

        assertFalse(first.getId().equals(second.getId()));
    }

    @Test
    void longStayDiscountIsAppliedToTheTotalPrice() throws Exception {
        LocalDate checkOut = DEC_10.plusDays(7);

        Reservation reservation = service.book(alice, 101, DEC_10, checkOut, 1, false);

        // 7 nights x 60 = 420, minus 10 %
        assertEquals(378.0, reservation.getTotalPrice(), DELTA);
    }

    @Test
    void quoteGivesThePriceWithoutBooking() throws Exception {
        double price = service.quote(202, DEC_10, DEC_12, 2, true);

        assertEquals((180.0 + 30.0) * 2, price, DELTA);
        assertTrue(service.findAllReservations().isEmpty());
    }

    @Test
    void availableRoomsExcludeBookedAndTooSmallRooms() throws Exception {
        service.book(alice, 103, DEC_10, DEC_15, 2, false);

        List<Room> available = service.findAvailableRooms(DEC_12, DEC_15, 2);

        // 101 is too small, 103 is booked: only the suite remains.
        assertEquals(1, available.size());
        assertEquals(202, available.get(0).getNumber());
    }

    @Test
    void customerReservationsAreFoundByEmailWhateverTheCase() throws Exception {
        service.book(alice, 101, DEC_10, DEC_12, 1, false);
        service.book(bob, 103, DEC_10, DEC_12, 1, false);

        List<Reservation> reservations = service.findByCustomerEmail("ALICE@mail.com");

        assertEquals(1, reservations.size());
        assertEquals(alice, reservations.get(0).getCustomer());
    }

    @Test
    void expectedRevenueIgnoresCancelledReservations() throws Exception {
        service.book(alice, 101, DEC_10, DEC_12, 1, false);               // 120
        Reservation toCancel = service.book(bob, 103, DEC_10, DEC_12, 1, false); // 180
        service.cancel(toCancel.getId());

        assertEquals(120.0, service.calculateExpectedRevenue(), DELTA);
    }

    // ----- Edge cases -----

    @Test
    void newGuestCanCheckInOnThePreviousGuestsCheckOutDay() throws Exception {
        service.book(alice, 101, DEC_10, DEC_12, 1, false);

        Reservation next = service.book(bob, 101, DEC_12, DEC_15, 1, false);

        assertTrue(next.isActive());
    }

    @Test
    void oneNightStayIsAllowed() throws Exception {
        Reservation reservation = service.book(alice, 101, DEC_10, DEC_10.plusDays(1), 1, false);

        assertEquals(1, reservation.getNights());
    }

    @Test
    void cancelledReservationFreesTheRoom() throws Exception {
        Reservation reservation = service.book(alice, 101, DEC_10, DEC_15, 1, false);
        service.cancel(reservation.getId());

        Reservation newOne = service.book(bob, 101, DEC_10, DEC_15, 1, false);

        assertTrue(newOne.isActive());
    }

    @Test
    void roomFilledToExactCapacityIsAccepted() throws Exception {
        Reservation reservation = service.book(alice, 202, DEC_10, DEC_12, 4, false);

        assertEquals(4, reservation.getGuests());
    }

    // ----- Error cases -----

    @Test
    void overlappingReservationIsRefused() throws Exception {
        service.book(alice, 101, DEC_10, DEC_15, 1, false);

        RoomNotAvailableException exception = assertThrows(RoomNotAvailableException.class,
                () -> service.book(bob, 101, DEC_12, DEC_15.plusDays(2), 1, false));

        assertTrue(exception.getMessage().contains("101"));
    }

    @Test
    void quoteForAnAlreadyBookedRoomIsRefused() throws Exception {
        service.book(alice, 101, DEC_10, DEC_15, 1, false);

        assertThrows(RoomNotAvailableException.class,
                () -> service.quote(101, DEC_12, DEC_15, 1, false));
    }

    @Test
    void refusedBookingDoesNotCreateAReservation() throws Exception {
        service.book(alice, 101, DEC_10, DEC_15, 1, false);

        assertThrows(RoomNotAvailableException.class,
                () -> service.book(bob, 101, DEC_12, DEC_15, 1, false));

        assertEquals(1, service.findAllReservations().size());
    }

    @Test
    void checkOutBeforeCheckInIsRefused() {
        assertThrows(InvalidReservationException.class,
                () -> service.book(alice, 101, DEC_15, DEC_10, 1, false));
    }

    @Test
    void zeroNightStayIsRefused() {
        assertThrows(InvalidReservationException.class,
                () -> service.book(alice, 101, DEC_10, DEC_10, 1, false));
    }

    @Test
    void tooManyGuestsIsRefused() {
        InvalidReservationException exception = assertThrows(InvalidReservationException.class,
                () -> service.book(alice, 103, DEC_10, DEC_12, 3, false));

        assertTrue(exception.getMessage().contains("at most 2"));
    }

    @Test
    void zeroGuestsIsRefused() {
        assertThrows(InvalidReservationException.class,
                () -> service.findAvailableRooms(DEC_10, DEC_12, 0));
    }

    @Test
    void unknownRoomIsRefused() {
        assertThrows(RoomNotFoundException.class,
                () -> service.book(alice, 999, DEC_10, DEC_12, 1, false));
    }

    @Test
    void cancellingAnUnknownReservationIsRefused() {
        assertThrows(ReservationNotFoundException.class, () -> service.cancel("R999"));
    }

    @Test
    void cancellingTwiceIsRefused() throws Exception {
        Reservation reservation = service.book(alice, 101, DEC_10, DEC_12, 1, false);
        service.cancel(reservation.getId());

        assertThrows(InvalidReservationException.class, () -> service.cancel(reservation.getId()));
    }
}
