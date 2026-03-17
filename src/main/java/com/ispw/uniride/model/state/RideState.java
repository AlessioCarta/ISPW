package com.ispw.uniride.model.state;

import com.ispw.uniride.model.Ride;

public interface RideState {
    boolean bookSeat(Ride ride);
    boolean cancelSeat(Ride ride);
    String getStatus();
}
