package com.hotel.repository;

import com.hotel.model.Reservation;

import java.util.List;
import java.util.Optional;

/** Storage of the reservations. */
public interface ReservationRepository {

    void save(Reservation reservation);

    Optional<Reservation> findById(String id);

    List<Reservation> findAll();

    List<Reservation> findByRoomNumber(int roomNumber);
}
