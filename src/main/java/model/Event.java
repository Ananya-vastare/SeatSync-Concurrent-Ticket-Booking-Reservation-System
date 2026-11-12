package model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Event {
    private final String eventId;
    private final String name;
    private final Map<String, Seat> seats = new LinkedHashMap<>();

    public Event(String eventId, String name, int numberOfSeats) {
        if (numberOfSeats <= 0) {
            throw new IllegalArgumentException("An event must have at least one seat.");
        }
        this.eventId = eventId;
        this.name = name;
        for (int i = 1; i <= numberOfSeats; i++) {
            String seatId = String.format("A%02d", i);
            seats.put(seatId, new Seat(seatId));
        }
    }

    public String getEventId() {
        return eventId;
    }

    public String getName() {
        return name;
    }

    public Seat getSeat(String seatId) {
        return seats.get(seatId == null ? null : seatId.trim().toUpperCase());
    }

    public Collection<Seat> getSeats() {
        return seats.values();
    }

    @Override
    public String toString() {
        return eventId + " - " + name;
    }
}
