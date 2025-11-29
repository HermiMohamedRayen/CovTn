package com.iset.covtn.controller;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.iset.covtn.models.RideParticipation;
import com.iset.covtn.repository.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iset.covtn.exceptions.UserDejaExistException;
import com.iset.covtn.models.UserInfo;
import com.iset.covtn.models.Ride;
import com.iset.covtn.service.UserInfoService;
import com.iset.covtn.service.RideService;


@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserInfoService userService;

    @Autowired
    private RideService rideService;
    @Autowired
    private RideRepository rideRepository;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * Liste de tous les utilisateurs (admin uniquement)
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserInfo>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }


    /**
     * Supprimer un utilisateur (admin uniquement)
     */
    @DeleteMapping("/users/{email}")
    public ResponseEntity<String> deleteUser(@PathVariable String email) {
        try {
            String result = userService.deleteUser(email);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur introuvable avec l'email : " + email);
        }
    }
    @PutMapping("/Updateusers")
    public ResponseEntity<Void> updateUser(@RequestBody UserInfo userInfo) {
        userService.updateUser(userInfo);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/users/{email}")
    public ResponseEntity<UserInfo> getUserByEmail(@PathVariable String email) {
        UserInfo user = userService.findByEmail(email);
        user.setPassword(""); // Ne pas exposer le mot de passe
        return ResponseEntity.ok(user);
    }
    @PostMapping("/addUser")
    public ResponseEntity<String> addUser(@RequestBody UserInfo userInfo) {
        try{
            return ResponseEntity.ok(userService.addUser(userInfo));
        } catch (UserDejaExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body( e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'ajout de l'utilisateur : " + e.getMessage());
        }
    }

    /**
     * CRUD pour les conducteurs (Driver)
     */


    @GetMapping("/drivers")
    public ResponseEntity<List<UserInfo>> getAllDrivers() {
        return ResponseEntity.ok(userService.getAllDrivers());
    }

    @PutMapping("/drivers/{id}")
    public ResponseEntity<String> updateDriver(@PathVariable String id, @RequestBody UserInfo driverInfo) {
        return ResponseEntity.ok(userService.updateDriver(id, driverInfo));
    }

    @DeleteMapping("/drivers/{email}")
    public ResponseEntity<String> deleteDriver(@PathVariable String email) {
        return ResponseEntity.ok(userService.deleteDriver(email));
    }


    @PostMapping("/passengers")
    public ResponseEntity<String> addPassenger(@RequestBody UserInfo passengerInfo) {
        return ResponseEntity.ok(userService.addPassenger(passengerInfo));
    }

    @GetMapping("/passengers")
    public ResponseEntity<List<UserInfo>> getAllPassengers() {
        return ResponseEntity.ok(userService.getAllPassengers());
    }

    @PutMapping("/passengers/{email}")
    public ResponseEntity<String> updatePassenger(@PathVariable String email, @RequestBody UserInfo passengerInfo) {
        return ResponseEntity.ok(userService.updatePassenger(email, passengerInfo));
    }

    @DeleteMapping("/passengers/{email}")
    public ResponseEntity<String> deletePassenger(@PathVariable String email) {
        return ResponseEntity.ok(userService.deletePassenger(email));
    }

    @GetMapping("/rides")
    public ResponseEntity<List<Ride>> getAllRides() {
        return ResponseEntity.ok(rideService.getAllRides());
    }

    @GetMapping("/rides/approved")
    public ResponseEntity<List<Ride>> getApprovedRides() {
        return ResponseEntity.ok(rideService.getApprovedRides());
    }

    @GetMapping("/rides/pending")
    public ResponseEntity<List<Ride>> getPendingRides() {
        return ResponseEntity.ok(rideService.getPendingRides());
    }

    @GetMapping("/rides/{id}")
    public ResponseEntity<Ride> getRideById(@PathVariable Long id) {
        return rideService.getRideById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/rides/{id}/approve")
    public ResponseEntity<Ride> approveRide(@PathVariable Long id) {
        try {
            Ride ride = rideService.approveRide(id);
            String depart = sdf.format(ride.getDeparture()) ;
            String arrive = sdf.format(ride.getArrivalTime());
            userService.sendMessage(ride.getDriver().getEmail(),"you have an approved ride that depart at "+depart+" and arrive at "+arrive);
            ride.getRideParticipations().forEach(participation -> {
                userService.sendMessage(participation.getRider().getEmail(),"a ride which you are participated in that depart at "+depart+" and arrive at "+arrive+" have been approved");
            });
            return ResponseEntity.ok(ride);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/rides/{id}/reject")
    public ResponseEntity<Ride> rejectRide(@PathVariable Long id) {
        try {
            Ride ride = rideService.rejectRide(id);
            String depart = sdf.format(ride.getDeparture()) ;
            String arrive = sdf.format(ride.getArrivalTime());
            userService.sendMessage(ride.getDriver().getEmail(),"you have a rejected ride that depart at "+depart+" and arrive at "+arrive);
            ride.getRideParticipations().forEach(participation -> {
                userService.sendMessage(participation.getRider().getEmail(),"a ride which you are participated in that depart at "+depart+" and arrive at "+arrive+" have been rejected");
            });

            return ResponseEntity.ok(ride);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/rides/{id}")
    public ResponseEntity<String> deleteRide(@PathVariable Long id) {
        try {
            Ride ride = rideRepository.findById(id).orElse(null);
            if(ride == null) {
                throw new Exception("Ride not found");
            }
            rideService.deleteRide(id);
            String depart = " ";
            String arrive = " ";
            try {
                depart = sdf.format(ride.getDepartureTime());
                arrive = sdf.format(ride.getArrivalTime());
            }catch (Exception e){
                System.err.println(e);
            }
            userService.sendMessage(ride.getDriver().getEmail(),"one of your rides that depart at "+depart+" and arrive at "+arrive+" has been deleted by the administrator");
            for(RideParticipation participation : ride.getRideParticipations()) {
                userService.sendMessage(participation.getRider().getEmail(), "a ride which you are participated in that depart at " + depart + " and arrive at " + arrive + " have been deleted");
            }
            return ResponseEntity.ok("Ride deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Ride not found with id: " + id);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalRides", rideService.getTotalRidesCount());
        stats.put("approvedRides", rideService.getApprovedRidesCount());
        stats.put("pendingRides", rideService.getPendingRidesCount());
        stats.put("totalUsers", (long) userService.getAllUsers().size());
        stats.put("totalDrivers", (long) userService.getAllDrivers().size());
        stats.put("totalPassengers", (long) userService.getAllPassengers().size());
        return ResponseEntity.ok(stats);
    }

}
