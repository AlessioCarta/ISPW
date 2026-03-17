package com.ispw.uniride.model.state;

import com.ispw.uniride.model.Ride;

public class AvailableState implements RideState {
    @Override
    public boolean bookSeat(Ride ride) {
        if (ride.getAvailableSeats() > 0) {
            ride.setAvailableSeats(ride.getAvailableSeats() - 1);
            if (ride.getAvailableSeats() == 0) {
                ride.setState(new FullState());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean cancelSeat(Ride ride) {
        if (ride.getAvailableSeats() < ride.getTotalSeats()) {
            ride.setAvailableSeats(ride.getAvailableSeats() + 1);
            return true;
        }
        return false; // Cannot cancel what hasn't been booked
    }

    @Override
    public String getStatus() {
        return "AVAILABLE";
    }
}
