package com.hotel.service;

import com.hotel.exception.InvalidReservationException;
import com.hotel.exception.ReservationNotFoundException;
import com.hotel.exception.RoomNotAvailableException;
import com.hotel.exception.RoomNotFoundException;
import com.hotel.model.Customer;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Business rules of the hotel: availability, booking and cancellation.
 *
 * All its collaborators are interfaces or services received through the
 * constructor, so it can be tested with any storage implementation.
 */
public class ReservationService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final PricingService pricingService;
    private int nextReservationNumber = 1;

    public ReservationService(RoomRepository roomRepository,
                              ReservationRepository reservationRepository,
                              PricingService pricingService) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.pricingService = pricingService;
    }

    /** Returns the rooms that can host the guests and are free for the whole period. */
    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, int guests) {
        validateDates(checkIn, checkOut);
        validateGuests(guests);
        return roomRepository.findAll().stream()
                .filter(room -> room.canAccommodate(guests))
                .filter(room -> isAvailable(room, checkIn, checkOut))
                .toList();
    }

    public boolean isAvailable(Room room, LocalDate checkIn, LocalDate checkOut) {
        // Cancelled reservations no longer block the room.
        return reservationRepository.findByRoomNumber(room.getNumber()).stream()
                .filter(Reservation::isActive)
                .noneMatch(reservation -> reservation.overlaps(checkIn, checkOut));
    }

    /**
     * Calculates the price of a stay without booking it.
     * The same rules as a booking are checked first, so a price is never
     * shown for a stay that would be refused.
     */
    public double quote(int roomNumber, LocalDate checkIn, LocalDate checkOut, int guests, boolean breakfast)
            throws RoomNotFoundException, RoomNotAvailableException {
        validateDates(checkIn, checkOut);
        Room room = findRoom(roomNumber);
        validateCapacity(room, guests);
        ensureAvailable(room, checkIn, checkOut);
        return pricingService.calculateTotal(room, nightsBetween(checkIn, checkOut), guests, breakfast);
    }

    public Reservation book(Customer customer, int roomNumber, LocalDate checkIn, LocalDate checkOut,
                            int guests, boolean breakfast)
            throws RoomNotFoundException, RoomNotAvailableException {
        if (customer == null) {
            throw new InvalidReservationException("A reservation needs a customer");
        }
        validateDates(checkIn, checkOut);
        Room room = findRoom(roomNumber);
        validateCapacity(room, guests);
        ensureAvailable(room, checkIn, checkOut);

        int nights = nightsBetween(checkIn, checkOut);
        double totalPrice = pricingService.calculateTotal(room, nights, guests, breakfast);
        Reservation reservation = new Reservation(generateId(), customer, room,
                checkIn, checkOut, guests, breakfast, totalPrice);
        reservationRepository.save(reservation);
        return reservation;
    }

    public Reservation cancel(String reservationId) throws ReservationNotFoundException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("No reservation with id " + reservationId));
        if (!reservation.isActive()) {
            throw new InvalidReservationException("Reservation " + reservationId + " is already cancelled");
        }
        reservation.cancel();
        return reservation;
    }

    public List<Reservation> findByCustomerEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        return reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getCustomer().getEmail().equals(normalizedEmail))
                .toList();
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Room> findAllRooms() {
        return roomRepository.findAll();
    }

    /** Sum of the prices of the confirmed reservations. */
    public double calculateExpectedRevenue() {
        return reservationRepository.findAll().stream()
                .filter(Reservation::isActive)
                .mapToDouble(Reservation::getTotalPrice)
                .sum();
    }

    private Room findRoom(int roomNumber) throws RoomNotFoundException {
        return roomRepository.findByNumber(roomNumber)
                .orElseThrow(() -> new RoomNotFoundException("Room " + roomNumber + " does not exist"));
    }

    private void ensureAvailable(Room room, LocalDate checkIn, LocalDate checkOut) throws RoomNotAvailableException {
        if (!isAvailable(room, checkIn, checkOut)) {
            throw new RoomNotAvailableException("Room " + room.getNumber() + " is already booked between "
                    + checkIn + " and " + checkOut);
        }
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new InvalidReservationException("Check-in and check-out dates are required");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidReservationException("Check-out (" + checkOut
                    + ") must be after check-in (" + checkIn + ")");
        }
    }

    private void validateGuests(int guests) {
        if (guests <= 0) {
            throw new InvalidReservationException("Number of guests must be positive");
        }
    }

    private void validateCapacity(Room room, int guests) {
        validateGuests(guests);
        if (!room.canAccommodate(guests)) {
            throw new InvalidReservationException("Room " + room.getNumber() + " can host at most "
                    + room.getCapacity() + " guest(s), requested: " + guests);
        }
    }

    private int nightsBetween(LocalDate checkIn, LocalDate checkOut) {
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    private String generateId() {
        return String.format("R%03d", nextReservationNumber++);
    }
}
