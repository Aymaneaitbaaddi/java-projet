package com.hotel.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * A booking of one room by one customer for a period of time.
 *
 * The check-out day is not counted as a night: a stay from the 10th to the 12th
 * is 2 nights, and another guest can arrive in the same room on the 12th.
 */
public class Reservation {

    private final String id;
    private final Customer customer;
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final int guests;
    private final boolean breakfast;
    private final double totalPrice;
    private ReservationStatus status;

    public Reservation(String id, Customer customer, Room room, LocalDate checkIn,
                       LocalDate checkOut, int guests, boolean breakfast, double totalPrice) {
        this.id = id;
        this.customer = customer;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guests = guests;
        this.breakfast = breakfast;
        this.totalPrice = totalPrice;
        this.status = ReservationStatus.CONFIRMED;
    }

    public long getNights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    /**
     * Two periods overlap when each one starts before the other ends.
     * Strict comparisons allow a check-in on the same day as a previous check-out.
     */
    public boolean overlaps(LocalDate otherCheckIn, LocalDate otherCheckOut) {
        return checkIn.isBefore(otherCheckOut) && otherCheckIn.isBefore(checkOut);
    }

    public boolean isActive() {
        return status == ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        if (!isActive()) {
            throw new IllegalStateException("Reservation " + id + " is already cancelled");
        }
        status = ReservationStatus.CANCELLED;
    }

    public String getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public int getGuests() {
        return guests;
    }

    public boolean hasBreakfast() {
        return breakfast;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - room %d (%s) - %s to %s (%d night(s)) - %d guest(s) - %.2f EUR - %s",
                id, customer.getName(), room.getNumber(), room.getType(), checkIn, checkOut,
                getNights(), guests, totalPrice, status);
    }
}
