package com.iset.covtn.service;

import com.iset.covtn.models.Ride;
import com.iset.covtn.repository.RideRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class RideService {

    private final RideRepository rideRepository;

    public RideService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    public Optional<Ride> getRideById(Long id) {
        return rideRepository.findById(id);
    }

    public List<Ride> getApprovedRides() {
        return rideRepository.findAll().stream()
                .filter(Ride::isApproved)
                .toList();
    }

    public List<Ride> getPendingRides() {
        return rideRepository.findAll().stream()
                .filter(ride -> !ride.isApproved())
                .toList();
    }

    public Ride approveRide(Long id) {
        Optional<Ride> ride = rideRepository.findById(id);
        if (ride.isPresent()) {
            Ride r = ride.get();
            r.setApproved(true);
            return rideRepository.save(r);
        }
        throw new RuntimeException("Ride not found with id: " + id);
    }

    public Ride rejectRide(Long id) {
        Optional<Ride> ride = rideRepository.findById(id);
        if (ride.isPresent()) {
            Ride r = ride.get();
            r.setApproved(false);
            return rideRepository.save(r);
        }
        throw new RuntimeException("Ride not found with id: " + id);
    }

    public void deleteRide(Long id) {
        rideRepository.deleteById(id);
    }

    public long getTotalRidesCount() {
        return rideRepository.count();
    }

    public long getApprovedRidesCount() {
        return getApprovedRides().size();
    }

    public long getPendingRidesCount() {
        return getPendingRides().size();
    }
}
