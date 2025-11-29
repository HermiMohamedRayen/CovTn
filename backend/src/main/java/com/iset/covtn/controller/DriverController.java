package com.iset.covtn.controller;

import com.iset.covtn.models.Car;
import com.iset.covtn.models.Ride;
import com.iset.covtn.models.RideParticipation;
import com.iset.covtn.models.UserInfo;
import com.iset.covtn.repository.RideRepository;
import com.iset.covtn.repository.UserInfoRepository;
import com.iset.covtn.service.FileSystemStorageService;
import com.iset.covtn.service.JwtService;
import com.iset.covtn.service.RideService;
import com.iset.covtn.service.UserInfoService;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/driver")
class DriverController {

    private final UserInfoService userInfoService;
    private final JwtService jwtService;
    private final FileSystemStorageService fileSystemStorageService;
    private final UserInfoRepository userInfoRepository;
    private final RideRepository rideRepository;
    private final RideService rideService;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");


    DriverController(UserInfoService userInfoService, JwtService jwtService, FileSystemStorageService fileSystemStorageService, UserInfoRepository userInfoRepository, RideRepository rideRepository, RideService rideService) {
        this.userInfoService = userInfoService;
        this.jwtService = jwtService;
        this.fileSystemStorageService = fileSystemStorageService;
        this.userInfoRepository = userInfoRepository;
        this.rideRepository = rideRepository;
        this.rideService = rideService;
    }

    @PostMapping("/proposeRide")
    public ResponseEntity<?> proposeRide(@RequestHeader("Authorization") String token,@RequestBody Ride ride) {
        ride.setApproved(false);
        token = token.substring(7);
        UserInfo user = userInfoService.findByEmail(jwtService.extractUsername(token));
        if(user.getCar()==null){
            return ResponseEntity.badRequest().body("add the car first");
        }
        ride.setDriver(user);
        rideRepository.save(ride);


        return ResponseEntity.ok().build();

    }

    @PostMapping("/car")
    public ResponseEntity<?> addCar(
            @RequestHeader("Authorization")  String token,
            @RequestParam("files")  List<MultipartFile> files,
            @RequestPart("car") Car car
            )
    {

        System.out.println(files.get(0).getOriginalFilename());

        if(files.isEmpty() || files.size() < 2){
            return ResponseEntity.badRequest().body("provide at least 2 images");
        }
        car.setPhotos(new ArrayList<>());
        String name ;
        for( MultipartFile file : files){
            name = fileSystemStorageService.store(file);
            car.getPhotos().add(name);
        }
        token = token.substring(7);

        token = jwtService.extractUsername(token);

        userInfoService.setCar(car,token);


        return ResponseEntity.ok().build();


    }

    @GetMapping("/car")
    public ResponseEntity<?> getCurrentUserCar(
            @RequestHeader("Authorization") String token
    ) {
        token = token.substring(7);
        String email = jwtService.extractUsername(token);
        UserInfo user = userInfoService.findByEmail(email);
        
        if (user == null || user.getCar() == null) {
            return ResponseEntity.status(404).body("Car not found");
        }

        return ResponseEntity.ok(user.getCar());
    }

    @GetMapping("/car/{carId}")
    public ResponseEntity<?> getCar(
            @RequestHeader("Authorization") String token,
            @PathVariable long carId
    ) {
        token = token.substring(7);
        String email = jwtService.extractUsername(token);
        UserInfo user = userInfoService.findByEmail(email);
        
        if (user == null || user.getCar() == null || user.getCar().getId() != carId) {
            return ResponseEntity.status(403).body("Unauthorized to access this car");
        }

        return ResponseEntity.ok(user.getCar());
    }

    @PutMapping("/car/{carId}")
    public ResponseEntity<?> updateCar(
            @RequestHeader("Authorization") String token,
            @PathVariable long carId,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestPart("car") Car car
    ) {
        token = token.substring(7);
        String email = jwtService.extractUsername(token);
        UserInfo user = userInfoService.findByEmail(email);
        
        if (user == null || user.getCar() == null || user.getCar().getId() != carId) {
            return ResponseEntity.status(403).body("Unauthorized to update this car");
        }

        Car existingCar = user.getCar();
        existingCar.setMatriculationNumber(car.getMatriculationNumber());
        existingCar.setModel(car.getModel());
        existingCar.setSeats(car.getSeats());
        existingCar.setAirConditioner(car.isAirConditioner());
        existingCar.setSmoker(car.isSmoker());

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String name = fileSystemStorageService.store(file);
                existingCar.getPhotos().add(name);
            }
        }

        if (car.getPhotosToRemove() != null && !car.getPhotosToRemove().isEmpty()) {
            existingCar.getPhotos().removeAll(car.getPhotosToRemove());
        }

        userInfoService.setCar(existingCar, email);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/rides")
    public ResponseEntity<?> getRides(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserInfo user = userInfoService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(user.getRides());
    }
/*
    @GetMapping("/getMyRideParticipers/{id}")
    public ResponseEntity<?> getMyRideParticiper(@PathVariable long id) {

        Optional<Ride> ride = rideRepository.findById(id);
        if(ride.isPresent()) {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserInfo user = userInfoService.findByEmail(userDetails.getUsername());
            if(ride.get().getDriver().getEmail().equals(user.getEmail())){
                return ResponseEntity.ok(ride.get().getRideParticipations());
            }
        }

        return ResponseEntity.notFound().build();
    }

 */



    @DeleteMapping("/ride/{id}")
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
            for(RideParticipation participation : ride.getRideParticipations()) {
                userInfoService.sendMessage(participation.getRider().getEmail(), "a ride which you are participated in that depart at " + depart + " and arrive at " + arrive + " have been deleted");
            }
            return ResponseEntity.ok("Ride deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Ride not found with id: " + id);
        }
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex)
    {
        System.err.println(ex);
        return ResponseEntity.badRequest().body(ex.getMessage());
    }



}
