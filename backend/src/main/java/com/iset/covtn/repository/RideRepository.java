package com.iset.covtn.repository;


import com.iset.covtn.models.Ride;
import com.iset.covtn.models.UserInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long> {

    float r = 2f;

    @Query("SELECT r FROM Ride r WHERE r.approved and SQRT( POWER((?1 - r.departure.latitude) * 111.32, 2) + POWER((?2 - r.departure.longitude) * 40075 * COS(((?1 - r.departure.latitude) / 2) * PI() / 180) / 360, 2)) <= "+r+" and SQRT( POWER((?3 - r.destination.latitude) * 111.32, 2) + POWER((?4 - r.destination.longitude) * 40075 * COS(((?3 - r.destination.latitude) / 2) * PI() / 180) / 360, 2)) <= "+r+" and  r.departureTime >= ?5 and r.arrivalTime <= ?6")
    List<Ride> findClosestRide(
           double deplat,
           double deplng,
            double destlat,
            double destlng,
           Date depTime,
           Date arrTime
           );

    @Query("select r from Ride r where r.approved and r.departureTime > now() order by r.id desc ")
    List<Ride> findRidesByApprovedOrderByIdDesc(Pageable pageable);

    @Query("SELECT r FROM Ride r WHERE r.approved  and SQRT( POWER((?1 - r.departure.latitude) * 111.32, 2) + POWER((?2 - r.departure.longitude) * 40075 * COS(((?1 - r.departure.latitude) / 2) * PI() / 180) / 360, 2)) <= "+r+" and r.departureTime > now() order by r.id desc")
    List<Ride> findApprovedLatestRideToUser(
            double deplat,
            double deplng,
            Pageable pageable
    );



}

