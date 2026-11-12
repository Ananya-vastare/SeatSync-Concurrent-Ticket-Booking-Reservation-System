# Concurrent Ticket Booking System (Java 17+)

A console-based Java project demonstrating object-oriented design, collections, queues,
file I/O, and thread-safe seat booking.

## Features
- View events and seat availability
- Book a seat and prevent duplicate bookings
- Cancel a booking
- Automatically promote the next waiting user when a seat is released
- View booking history
- Persist confirmed bookings in `data/bookings.csv`
- Run a two-thread demonstration to show that only one user can book a seat

## Requirements
- JDK 17 or newer
- No external libraries or build tools required

## Run in IntelliJ IDEA
1. Open this folder as a project.
2. Ensure the project SDK is JDK 17 or newer.
3. Mark `src/main/java` as the Sources Root if IntelliJ does not do so automatically.
4. Run `Main.java`.

## Run from a terminal
From the project root:

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out Main
```

On Windows PowerShell, compile with:

```powershell
Get-ChildItem -Recurse src/main/java -Filter *.java | ForEach-Object { $_.FullName } | Set-Content sources.txt
javac -d out @sources.txt
java -cp out Main
```

## Concurrency design
`BookingService.bookSeat()` and `cancelBooking()` are synchronized. The seat availability
check and status update happen inside the same synchronized operation, so two threads
cannot both successfully book the same seat through this service instance.

This is an in-memory demonstration, not a production ticketing platform. A production
system would normally use database transactions, locking, payment handling, and stronger
persistence guarantees.

## Project structure

```text
ConcurrentTicketBooking/
├── data/
│   └── bookings.csv          # Created automatically
├── src/main/java/
│   ├── Main.java
│   ├── enums/
│   │   ├── BookingStatus.java
│   │   └── SeatStatus.java
│   ├── model/
│   │   ├── Booking.java
│   │   ├── Event.java
│   │   ├── Seat.java
│   │   └── User.java
│   └── service/
│       ├── BookingService.java
│       └── EventService.java
└── README.md
```
