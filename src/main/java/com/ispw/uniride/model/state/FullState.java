package com.ispw.uniride.model.state;

import com.ispw.uniride.model.Ride;

public class FullState implements RideState {
    @Override
    public boolean bookSeat(Ride ride) {
        return false; // Cannot book, it's full
    }

    @Override
    public boolean cancelSeat(Ride ride) {
        if (ride.getAvailableSeats() < ride.getTotalSeats()) {
            ride.setAvailableSeats(ride.getAvailableSeats() + 1);
            ride.setState(new AvailableState());
            return true;
        }
        return false;
    }

    @Override
    public String getStatus() {
        return "FULL";
    }
}
