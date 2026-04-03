package com.ispw.uniride.dao.memory;

import com.ispw.uniride.dao.RideDAO;
import com.ispw.uniride.model.Ride;

import java.util.ArrayList;
import java.util.List;

public class MemoryRideDAO implements RideDAO {
    private MemoryStorage storage;

    public MemoryRideDAO() {
        this.storage = MemoryStorage.getInstance();
    }

    @Override
    public void saveRide(Ride ride) {
        storage.getRidesMap().put(ride.getId(), ride);
    }

    @Override
    public List<Ride> getAllRides() {
        return storage.getRidesList();
    }

    @Override
    public List<Ride> getAvailableRides(String departure, String destination) {
        List<Ride> availableRides = new ArrayList<>();
        for (Ride ride : storage.getRidesList()) {
            if ("AVAILABLE".equals(ride.getStatus()) &&
                ride.getDeparture().equalsIgnoreCase(departure) &&
                ride.getDestination().equalsIgnoreCase(destination)) {
                availableRides.add(ride);
            }
        }
        return availableRides;
    }

    @Override
    public Ride getRideById(String id) {
        return storage.getRidesMap().get(id);
    }

    @Override
    public void updateRide(Ride ride) {
        storage.getRidesMap().put(ride.getId(), ride);
    }

    @Override
    public List<Ride> getRidesByDriver(String username) {
        List<Ride> result = new ArrayList<>();
        for (Ride ride : storage.getRidesList()) {
            if (ride.getDriverUsername().equals(username)) {
                result.add(ride);
            }
        }
        return result;
    }

    @Override
    public List<Ride> getRidesByPassenger(String username) {
        List<Ride> result = new ArrayList<>();
        for (Ride ride : storage.getRidesList()) {
            if (ride.getPassengerUsernames().contains(username)) {
                result.add(ride);
            }
        }
        return result;
    }
}
