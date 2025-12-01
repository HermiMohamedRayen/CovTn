package com.iset.covtn.repository;

import com.iset.covtn.models.RideParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RideParticipationRepository extends JpaRepository<RideParticipation, Long> {
    Optional<RideParticipation> findByRideId(Long rideId);

    Optional<RideParticipation> findByRideIdAndRiderEmail(Long rideId, String riderEmail);
}
