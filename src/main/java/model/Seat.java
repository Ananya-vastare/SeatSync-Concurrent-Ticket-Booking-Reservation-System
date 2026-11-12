package model;

import enums.SeatStatus;

public class Seat {
    private final String seatId;
    private SeatStatus status;

    public Seat(String seatId) {
        this.seatId = seatId;
        this.status = SeatStatus.AVAILABLE;
    }

    public String getSeatId() {
        return seatId;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return seatId + " [" + status + "]";
    }
}
