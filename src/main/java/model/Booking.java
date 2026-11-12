package model;

import enums.BookingStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Booking {
    private final String bookingId;
    private final User user;
    private final String eventId;
    private final String eventName;
    private final String seatId;
    private final LocalDateTime createdAt;
    private BookingStatus status;

    public Booking(String bookingId, User user, String eventId, String eventName,
                   String seatId, BookingStatus status) {
        this.bookingId = bookingId;
        this.user = user;
        this.eventId = eventId;
        this.eventName = eventName;
        this.seatId = seatId;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public String getBookingId() { return bookingId; }
    public User getUser() { return user; }
    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getSeatId() { return seatId; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %s | %s",
                bookingId, user.getName(), eventName, seatId, status,
                createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
