package service;

import enums.BookingStatus;
import enums.SeatStatus;
import model.Booking;
import model.Event;
import model.Seat;
import model.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BookingService {
    private final List<Booking> bookings = new ArrayList<>();
    private final Map<String, Deque<User>> waitingLists = new HashMap<>();
    private final AtomicInteger bookingSequence = new AtomicInteger(1000);
    private final Path storageFile;
    private final EventService eventService;

    public BookingService(EventService eventService, Path storageFile) {
        this.eventService = eventService;
        this.storageFile = storageFile;
    }

    // The availability check and booking update are protected by one lock.
    public synchronized Booking bookSeat(User user, String eventId, String seatId) {
        Event event = eventService.getEvent(eventId);
        if (event == null) {
            System.out.println("Event not found.");
            return null;
        }

        Seat seat = event.getSeat(seatId);
        if (seat == null) {
            System.out.println("Seat not found.");
            return null;
        }

        if (!seat.isAvailable()) {
            waitingLists.computeIfAbsent(waitingKey(eventId, seatId), key -> new ArrayDeque<>())
                    .addLast(user);
            System.out.println("Seat is already booked. " + user.getName()
                    + " has been added to the waiting list.");
            return null;
        }

        seat.setStatus(SeatStatus.BOOKED);
        Booking booking = new Booking(nextBookingId(), user, event.getEventId(),
                event.getName(), seat.getSeatId(), BookingStatus.CONFIRMED);
        bookings.add(booking);
        System.out.println("Booking confirmed: " + booking.getBookingId());
        return booking;
    }

    public synchronized boolean cancelBooking(String bookingId) {
        Booking booking = findBooking(bookingId);
        if (booking == null || booking.getStatus() != BookingStatus.CONFIRMED) {
            System.out.println("Active booking not found.");
            return false;
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Event event = eventService.getEvent(booking.getEventId());
        Seat seat = event == null ? null : event.getSeat(booking.getSeatId());

        if (seat != null) {
            Deque<User> queue = waitingLists.get(waitingKey(booking.getEventId(), booking.getSeatId()));
            if (queue != null && !queue.isEmpty()) {
                User nextUser = queue.removeFirst();
                Booking replacement = new Booking(nextBookingId(), nextUser,
                        event.getEventId(), event.getName(), seat.getSeatId(),
                        BookingStatus.CONFIRMED);
                bookings.add(replacement);
                // Seat remains BOOKED; ownership is transferred atomically.
                System.out.println("Seat " + seat.getSeatId() + " allocated to waiting user "
                        + nextUser.getName() + ". New booking: " + replacement.getBookingId());
                if (queue.isEmpty()) {
                    waitingLists.remove(waitingKey(booking.getEventId(), booking.getSeatId()));
                }
            } else {
                seat.setStatus(SeatStatus.AVAILABLE);
            }
        }

        System.out.println("Booking " + bookingId + " cancelled.");
        return true;
    }

    public synchronized List<Booking> getBookingsForUser(String userId) {
        List<Booking> result = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getUser().getUserId().equalsIgnoreCase(userId)) {
                result.add(booking);
            }
        }
        return result;
    }

    public synchronized List<Booking> getAllBookings() {
        return new ArrayList<>(bookings);
    }

    public synchronized int getWaitingListSize(String eventId, String seatId) {
        Deque<User> queue = waitingLists.get(waitingKey(eventId, seatId));
        return queue == null ? 0 : queue.size();
    }

    public synchronized void saveBookings() throws IOException {
        Path parent = storageFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(storageFile,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("bookingId,userId,userName,eventId,eventName,seatId,status");
            writer.newLine();

            for (Booking b : bookings) {
                // Persist all statuses so cancellation history is retained.
                writer.write(csv(b.getBookingId()) + "," + csv(b.getUser().getUserId()) + ","
                        + csv(b.getUser().getName()) + "," + csv(b.getEventId()) + ","
                        + csv(b.getEventName()) + "," + csv(b.getSeatId()) + ","
                        + b.getStatus().name());
                writer.newLine();
            }
        }
    }

    public synchronized void loadBookings() throws IOException {
        if (!Files.exists(storageFile)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(storageFile)) {
            String line = reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (fields.length != 7) {
                    continue;
                }

                try {
                    String bookingId = fields[0];
                    User user = new User(fields[1], fields[2]);
                    String eventId = fields[3];
                    String eventName = fields[4];
                    String seatId = fields[5];
                    BookingStatus status = BookingStatus.valueOf(fields[6]);

                    Booking booking = new Booking(bookingId, user, eventId,
                            eventName, seatId, status);
                    bookings.add(booking);

                    int numericId = Integer.parseInt(bookingId.substring(1));
                    bookingSequence.set(Math.max(bookingSequence.get(), numericId));

                    if (status == BookingStatus.CONFIRMED) {
                        Event event = eventService.getEvent(eventId);
                        Seat seat = event == null ? null : event.getSeat(seatId);
                        if (seat != null) {
                            seat.setStatus(SeatStatus.BOOKED);
                        }
                    }
                } catch (RuntimeException ignored) {
                    // Skip malformed or incompatible records instead of stopping startup.
                }
            }
        }
    }

    private Booking findBooking(String bookingId) {
        for (Booking booking : bookings) {
            if (booking.getBookingId().equalsIgnoreCase(bookingId)) {
                return booking;
            }
        }
        return null;
    }

    private String nextBookingId() {
        return "B" + bookingSequence.incrementAndGet();
    }

    private String waitingKey(String eventId, String seatId) {
        return eventId.trim().toUpperCase() + ":" + seatId.trim().toUpperCase();
    }

    private String csv(String value) {
        // Keep the simple CSV format safe for this console project.
        return value.replace(",", " ").replace("\n", " ").replace("\r", " ");
    }
}
