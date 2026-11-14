package com.hotel.repository;

import com.hotel.model.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/** Keeps the rooms in memory, indexed by room number. */
public class InMemoryRoomRepository implements RoomRepository {

    // TreeMap keeps the rooms sorted by number, which gives a clean display order.
    private final Map<Integer, Room> rooms = new TreeMap<>();

    @Override
    public void save(Room room) {
        rooms.put(room.getNumber(), room);
    }

    @Override
    public Optional<Room> findByNumber(int number) {
        return Optional.ofNullable(rooms.get(number));
    }

    @Override
    public List<Room> findAll() {
        return new ArrayList<>(rooms.values());
    }
}
