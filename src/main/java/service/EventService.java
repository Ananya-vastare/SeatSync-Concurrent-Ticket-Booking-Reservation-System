package service;

import model.Event;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class EventService {
    private final Map<String, Event> events = new LinkedHashMap<>();

    public void addEvent(Event event) {
        events.put(event.getEventId(), event);
    }

    public Event getEvent(String eventId) {
        return events.get(eventId == null ? null : eventId.trim().toUpperCase());
    }

    public Collection<Event> getAllEvents() {
        return events.values();
    }
}
