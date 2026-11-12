import enums.SeatStatus;
import model.Booking;
import model.Event;
import model.Seat;
import model.User;
import service.BookingService;
import service.EventService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        EventService eventService = new EventService();
        Event movie = new Event("E01", "Java Cinema: The Movie", 10);
        eventService.addEvent(movie);

        BookingService bookingService =
                new BookingService(eventService, Path.of("data", "bookings.csv"));

        try {
            bookingService.loadBookings();
        } catch (IOException e) {
            System.out.println("Could not load saved bookings: " + e.getMessage());
        }

        System.out.println("Welcome to the Concurrent Ticket Booking System!");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> viewEvents(eventService);
                case 2 -> viewSeats(movie);
                case 3 -> bookTicket(bookingService);
                case 4 -> cancelTicket(bookingService);
                case 5 -> viewBookingHistory(bookingService);
                case 6 -> viewWaitingList(bookingService);
                case 7 -> runConcurrencyDemo(bookingService, movie);
                case 8 -> {
                    save(bookingService);
                    running = false;
                    System.out.println("Thank you for using the booking system!");
                }
                default -> System.out.println("Invalid option. Please choose 1–8.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n========== TICKET BOOKING SYSTEM ==========");
        System.out.println("1. View Events");
        System.out.println("2. View Seats");
        System.out.println("3. Book Ticket");
        System.out.println("4. Cancel Booking");
        System.out.println("5. View Booking History");
        System.out.println("6. View Waiting List Size");
        System.out.println("7. Run Concurrency Demo");
        System.out.println("8. Save and Exit");
    }

    private static void viewEvents(EventService service) {
        System.out.println("\nAvailable events:");
        service.getAllEvents().forEach(System.out::println);
    }

    private static void viewSeats(Event event) {
        System.out.println("\nSeats for " + event.getName() + ":");
        for (Seat seat : event.getSeats()) {
            String marker = seat.getStatus() == SeatStatus.AVAILABLE ? "[AVAILABLE]" : "[BOOKED]";
            System.out.println(seat.getSeatId() + " " + marker);
        }
    }

    private static void bookTicket(BookingService service) {
        String userId = readText("User ID: ");
        String name = readText("Your name: ");
        String eventId = readText("Event ID (E01): ");
        String seatId = readText("Seat ID (e.g. A01): ");

        service.bookSeat(new User(userId, name), eventId, seatId);
    }

    private static void cancelTicket(BookingService service) {
        String bookingId = readText("Booking ID to cancel (e.g. B1001): ");
        service.cancelBooking(bookingId);
    }

    private static void viewBookingHistory(BookingService service) {
        String userId = readText("User ID: ");
        List<Booking> history = service.getBookingsForUser(userId);

        if (history.isEmpty()) {
            System.out.println("No booking history found.");
            return;
        }

        System.out.println("\nBooking history:");
        history.forEach(System.out::println);
    }

    private static void viewWaitingList(BookingService service) {
        String eventId = readText("Event ID: ");
        String seatId = readText("Seat ID: ");
        System.out.println("People waiting for " + seatId + ": "
                + service.getWaitingListSize(eventId, seatId));
    }

    private static void runConcurrencyDemo(BookingService service, Event event) {
        System.out.println("\nConcurrency demo: two users will try to book A10.");
        Seat seat = event.getSeat("A10");
        if (seat == null || !seat.isAvailable()) {
            System.out.println("A10 is not available. Reset the demo with a fresh data file or choose another seat.");
            return;
        }

        Runnable first = () -> service.bookSeat(new User("THREAD-1", "Thread User 1"),
                event.getEventId(), "A10");
        Runnable second = () -> service.bookSeat(new User("THREAD-2", "Thread User 2"),
                event.getEventId(), "A10");

        Thread t1 = new Thread(first, "Booking-Thread-1");
        Thread t2 = new Thread(second, "Booking-Thread-2");

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Concurrency demo interrupted.");
        }

        System.out.println("Demo complete. Only one thread can confirm the seat.");
    }

    private static void save(BookingService service) {
        try {
            service.saveBookings();
            System.out.println("Bookings saved successfully.");
        } catch (IOException e) {
            System.out.println("Could not save bookings: " + e.getMessage());
        }
    }

    private static String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
