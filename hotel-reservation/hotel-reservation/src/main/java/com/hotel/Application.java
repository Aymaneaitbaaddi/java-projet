package com.hotel;

import com.hotel.exception.DataLoadingException;
import com.hotel.loader.RoomCsvLoader;
import com.hotel.model.Room;
import com.hotel.pricing.LongStayDiscount;
import com.hotel.repository.InMemoryReservationRepository;
import com.hotel.repository.InMemoryRoomRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.PricingService;
import com.hotel.service.ReservationService;
import com.hotel.ui.ConsoleMenu;

import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point. Its only job is to assemble the objects (dependency injection by hand)
 * and start the menu: it contains no business logic.
 *
 * Usage:
 *   - no argument: loads rooms.csv from src/main/resources
 *   - one argument: path to another CSV file
 */
public class Application {

    private static final String DEFAULT_DATA_FILE = "rooms.csv";

    public static void main(String[] args) {
        RoomCsvLoader loader = new RoomCsvLoader();
        List<Room> rooms;
        try {
            rooms = args.length > 0
                    ? loader.loadFromFile(Path.of(args[0]))
                    : loader.loadFromClasspath(DEFAULT_DATA_FILE);
        } catch (DataLoadingException exception) {
            // Without rooms the application cannot work: explain why and stop.
            System.err.println("Cannot start the application: " + exception.getMessage());
            System.exit(1);
            return;
        }

        RoomRepository roomRepository = new InMemoryRoomRepository();
        rooms.forEach(roomRepository::save);
        System.out.println(rooms.size() + " rooms loaded.");

        // Business choice: 10 % discount from 7 nights. Changing the policy only means
        // passing another DiscountPolicy implementation here.
        PricingService pricingService = new PricingService(new LongStayDiscount(7, 0.10));
        ReservationService reservationService = new ReservationService(
                roomRepository, new InMemoryReservationRepository(), pricingService);

        new ConsoleMenu(reservationService, new Scanner(System.in), System.out).run();
    }
}
