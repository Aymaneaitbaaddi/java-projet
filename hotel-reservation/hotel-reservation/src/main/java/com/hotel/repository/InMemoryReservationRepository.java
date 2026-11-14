package com.hotel.repository;

import com.hotel.model.Reservation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Keeps the reservations in memory, indexed by reservation id. */
public class InMemoryReservationRepository implements ReservationRepository {

    // LinkedHashMap keeps the reservations in the order they were created.
    private final Map<String, Reservation> reservations = new LinkedHashMap<>();

    @Override
    public void save(Reservation reservation) {
        reservations.put(reservation.getId(), reservation);
    }

    @Override
    public Optional<Reservation> findById(String id) {
        return Optional.ofNullable(reservations.get(id));
    }

    @Override
    public List<Reservation> findAll() {
        return new ArrayList<>(reservations.values());
    }

    @Override
    public List<Reservation> findByRoomNumber(int roomNumber) {
        return reservations.values().stream()
                .filter(reservation -> reservation.getRoom().getNumber() == roomNumber)
                .toList();
    }
}
