package service;

import enums.BookingStatus;
import enums.SeatStatus;
import model.Booking;
import model.Event;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {
    @TempDir
    Path tempDir;

    private EventService eventService;
    private BookingService bookingService;
    private Event event;
    private Path storageFile;

    @BeforeEach
    void setUp() {
        eventService = new EventService();
        event = new Event("E01", "Test Event", 3);
        eventService.addEvent(event);
        storageFile = tempDir.resolve("bookings.csv");
        bookingService = new BookingService(eventService, storageFile);
    }

    @Test
    void bookAvailableSeatShouldConfirmBookingAndMarkSeatBooked() {
        Booking booking = bookingService.bookSeat(new User("U1", "Asha"), "E01", "A01");

        assertNotNull(booking);
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(SeatStatus.BOOKED, event.getSeat("A01").getStatus());
        assertEquals(1, bookingService.getAllBookings().size());
    }

    @Test
    void bookingUnknownEventShouldNotCreateBooking() {
        Booking booking = bookingService.bookSeat(new User("U1", "Asha"), "UNKNOWN", "A01");

        assertNull(booking);
        assertTrue(bookingService.getAllBookings().isEmpty());
    }

    @Test
    void bookingUnknownSeatShouldNotCreateBooking() {
        Booking booking = bookingService.bookSeat(new User("U1", "Asha"), "E01", "Z99");

        assertNull(booking);
        assertTrue(bookingService.getAllBookings().isEmpty());
    }

    @Test
    void bookingAlreadyBookedSeatShouldAddUserToWaitingList() {
        bookingService.bookSeat(new User("U1", "Asha"), "E01", "A01");

        Booking secondBooking = bookingService.bookSeat(new User("U2", "Ravi"), "E01", "A01");

        assertNull(secondBooking);
        assertEquals(1, bookingService.getWaitingListSize("E01", "A01"));
        assertEquals(1, bookingService.getAllBookings().size());
    }

    @Test
    void cancellingBookingShouldPromoteFirstWaitingUser() {
        Booking first = bookingService.bookSeat(new User("U1", "Asha"), "E01", "A01");
        bookingService.bookSeat(new User("U2", "Ravi"), "E01", "A01");

        assertTrue(bookingService.cancelBooking(first.getBookingId()));

        List<Booking> history = bookingService.getAllBookings();
        assertEquals(BookingStatus.CANCELLED, history.get(0).getStatus());
        assertEquals(BookingStatus.CONFIRMED, history.get(1).getStatus());
        assertEquals("U2", history.get(1).getUser().getUserId());
        assertEquals(SeatStatus.BOOKED, event.getSeat("A01").getStatus());
        assertEquals(0, bookingService.getWaitingListSize("E01", "A01"));
    }

    @Test
    void cancellingUnknownBookingShouldReturnFalse() {
        assertFalse(bookingService.cancelBooking("B9999"));
    }

    @Test
    void saveAndLoadShouldRestoreConfirmedBookingsAndSeatStatus() throws IOException {
        bookingService.bookSeat(new User("U1", "Asha"), "E01", "A02");
        bookingService.saveBookings();

        EventService newEventService = new EventService();
        Event newEvent = new Event("E01", "Test Event", 3);
        newEventService.addEvent(newEvent);
        BookingService restoredService = new BookingService(newEventService, storageFile);
        restoredService.loadBookings();

        assertEquals(1, restoredService.getAllBookings().size());
        assertEquals(BookingStatus.CONFIRMED, restoredService.getAllBookings().get(0).getStatus());
        assertEquals(SeatStatus.BOOKED, newEvent.getSeat("A02").getStatus());
    }

    @Test
    void simultaneousRequestsForSameSeatShouldNeverCreateTwoConfirmedBookings() throws Exception {
        int numberOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch ready = new CountDownLatch(numberOfThreads);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Booking> first = executor.submit(() -> {
                ready.countDown();
                start.await();
                return bookingService.bookSeat(new User("U1", "Asha"), "E01", "A03");
            });
            Future<Booking> second = executor.submit(() -> {
                ready.countDown();
                start.await();
                return bookingService.bookSeat(new User("U2", "Ravi"), "E01", "A03");
            });

            ready.await();
            start.countDown();

            int confirmedResults = (first.get() != null ? 1 : 0) + (second.get() != null ? 1 : 0);
            long confirmedInHistory = bookingService.getAllBookings().stream()
                    .filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();

            assertEquals(1, confirmedResults);
            assertEquals(1, confirmedInHistory);
            assertEquals(1, bookingService.getWaitingListSize("E01", "A03"));
        } finally {
            executor.shutdownNow();
        }
    }
}
