package com.iset.covtn.controller;

import com.iset.covtn.models.Car;
import com.iset.covtn.models.Ride;
import com.iset.covtn.models.UserInfo;
import com.iset.covtn.repository.RideRepository;
import com.iset.covtn.repository.UserInfoRepository;
import com.iset.covtn.service.FileSystemStorageService;
import com.iset.covtn.service.JwtService;
import com.iset.covtn.service.UserInfoService;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/driver")
class DriverController {

    private final UserInfoService userInfoService;
    private final JwtService jwtService;
    private final FileSystemStorageService fileSystemStorageService;
    private final UserInfoRepository userInfoRepository;
    private final RideRepository rideRepository;

    DriverController(UserInfoService userInfoService, JwtService jwtService, FileSystemStorageService fileSystemStorageService, UserInfoRepository userInfoRepository, RideRepository rideRepository) {
        this.userInfoService = userInfoService;
        this.jwtService = jwtService;
        this.fileSystemStorageService = fileSystemStorageService;
        this.userInfoRepository = userInfoRepository;
        this.rideRepository = rideRepository;
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex)
    {
        System.err.println(ex);
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

}
