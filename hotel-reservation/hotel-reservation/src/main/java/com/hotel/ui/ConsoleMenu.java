package com.hotel.ui;

import com.hotel.exception.InvalidReservationException;
import com.hotel.exception.ReservationNotFoundException;
import com.hotel.exception.RoomNotAvailableException;
import com.hotel.exception.RoomNotFoundException;
import com.hotel.model.Customer;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.service.ReservationService;

import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Console user interface. Only responsible for reading the user's input and
 * displaying results: every business rule lives in ReservationService.
 * Business errors are caught here, at the application boundary, and shown to the user.
 */
public class ConsoleMenu {

    private final ReservationService reservationService;
    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleMenu(ReservationService reservationService, Scanner scanner, PrintStream out) {
        this.reservationService = reservationService;
        this.scanner = scanner;
        this.out = out;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = readLine("Your choice: ");
            if (choice == null) {
                break; // end of input
            }
            try {
                switch (choice.trim()) {
                    case "1" -> listRooms();
                    case "2" -> searchAvailableRooms();
                    case "3" -> bookRoom();
                    case "4" -> cancelReservation();
                    case "5" -> showCustomerReservations();
                    case "6" -> showAllReservations();
                    case "0" -> running = false;
                    default -> out.println("Unknown option, please try again.");
                }
            } catch (IllegalStateException exception) {
                // The input stream was closed in the middle of an operation (e.g. Ctrl+D).
                out.println();
                out.println("Input closed, stopping the application.");
                running = false;
            }
        }
        out.println("Goodbye!");
    }

    private void printMenu() {
        out.println();
        out.println("===== HOTEL RESERVATION =====");
        out.println("1. List all rooms");
        out.println("2. Search available rooms");
        out.println("3. Book a room");
        out.println("4. Cancel a reservation");
        out.println("5. Show a customer's reservations");
        out.println("6. Show all reservations and revenue");
        out.println("0. Quit");
    }

    private void listRooms() {
        reservationService.findAllRooms().forEach(out::println);
    }

    private void searchAvailableRooms() {
        try {
            LocalDate checkIn = readDate("Check-in date (YYYY-MM-DD): ");
            LocalDate checkOut = readDate("Check-out date (YYYY-MM-DD): ");
            int guests = readInt("Number of guests: ");

            List<Room> available = reservationService.findAvailableRooms(checkIn, checkOut, guests);
            if (available.isEmpty()) {
                out.println("No room available for these dates.");
            } else {
                out.println("Available rooms:");
                available.forEach(room -> out.println("  " + room));
            }
        } catch (InvalidReservationException exception) {
            out.println("Invalid request: " + exception.getMessage());
        }
    }

    private void bookRoom() {
        try {
            String name = readLine("Customer name: ");
            String email = readLine("Customer email: ");
            Customer customer = new Customer(name, email);

            int roomNumber = readInt("Room number: ");
            LocalDate checkIn = readDate("Check-in date (YYYY-MM-DD): ");
            LocalDate checkOut = readDate("Check-out date (YYYY-MM-DD): ");
            int guests = readInt("Number of guests: ");
            boolean breakfast = readYesNo("Breakfast? (y/n): ");

            double price = reservationService.quote(roomNumber, checkIn, checkOut, guests, breakfast);
            out.printf("Total price: %.2f EUR%n", price);
            if (!readYesNo("Confirm the booking? (y/n): ")) {
                out.println("Booking cancelled by the user.");
                return;
            }

            Reservation reservation = reservationService.book(customer, roomNumber, checkIn, checkOut,
                    guests, breakfast);
            out.println("Booking confirmed: " + reservation);
        } catch (RoomNotAvailableException | RoomNotFoundException exception) {
            out.println("Booking refused: " + exception.getMessage());
        } catch (InvalidReservationException | IllegalArgumentException exception) {
            // Invalid dates/capacity (business rule) or invalid customer data (name, email).
            out.println("Invalid request: " + exception.getMessage());
        }
    }

    private void cancelReservation() {
        String id = readLine("Reservation id (e.g. R001): ");
        try {
            Reservation reservation = reservationService.cancel(id.trim().toUpperCase());
            out.println("Reservation cancelled: " + reservation);
        } catch (ReservationNotFoundException | InvalidReservationException exception) {
            out.println("Cancellation refused: " + exception.getMessage());
        }
    }

    private void showCustomerReservations() {
        String email = readLine("Customer email: ");
        List<Reservation> reservations = reservationService.findByCustomerEmail(email);
        if (reservations.isEmpty()) {
            out.println("No reservation for " + email);
        } else {
            reservations.forEach(out::println);
        }
    }

    private void showAllReservations() {
        List<Reservation> reservations = reservationService.findAllReservations();
        if (reservations.isEmpty()) {
            out.println("No reservation yet.");
        } else {
            reservations.forEach(out::println);
        }
        out.printf("Expected revenue (confirmed reservations): %.2f EUR%n",
                reservationService.calculateExpectedRevenue());
    }

    // ----- Input helpers: they ask again until the input is valid -----

    private String readLine(String prompt) {
        out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine() : null;
    }

    private int readInt(String prompt) {
        while (true) {
            String input = requireInput(prompt);
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException exception) {
                out.println("Please enter a whole number.");
            }
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            String input = requireInput(prompt);
            try {
                return LocalDate.parse(input.trim());
            } catch (DateTimeParseException exception) {
                out.println("Invalid date, expected format YYYY-MM-DD (e.g. 2026-12-24).");
            }
        }
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            String input = requireInput(prompt).trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            }
            if (input.equals("n") || input.equals("no")) {
                return false;
            }
            out.println("Please answer y or n.");
        }
    }

    private String requireInput(String prompt) {
        String input = readLine(prompt);
        if (input == null) {
            throw new IllegalStateException("No more input available");
        }
        return input;
    }
}
