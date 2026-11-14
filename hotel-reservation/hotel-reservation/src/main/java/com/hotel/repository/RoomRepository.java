package com.hotel.repository;

import com.hotel.model.Room;

import java.util.List;
import java.util.Optional;

/**
 * Storage of the hotel rooms.
 * The services depend on this interface, not on a concrete storage
 * (Dependency Inversion principle): it could be replaced by a database later.
 */
public interface RoomRepository {

    void save(Room room);

    Optional<Room> findByNumber(int number);

    List<Room> findAll();
}
