package com.ispw.uniride.dao;

import com.ispw.uniride.model.Ride;
import java.util.List;

public interface RideDAO {
    void saveRide(Ride ride);
    List<Ride> getAllRides();
    List<Ride> getAvailableRides(String departure, String destination);
    Ride getRideById(String id);
    void updateRide(Ride ride);
}
