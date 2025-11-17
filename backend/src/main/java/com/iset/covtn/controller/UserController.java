package com.iset.covtn.controller;

import com.iset.covtn.models.Ride;
import com.iset.covtn.models.UserInfo;
import com.iset.covtn.repository.RideRepository;
import com.iset.covtn.repository.UserInfoRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.iset.covtn.exceptions.StorageException;
import com.iset.covtn.service.FileSystemStorageService;
import com.iset.covtn.service.JwtService;
import com.iset.covtn.service.UserInfoService;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private FileSystemStorageService fileStorageService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserInfoService userService;
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private UserInfoRepository userInfoRepository;


    @PostMapping("/profile/updatePicture")
    public ResponseEntity<String> updateProfilePicture(@RequestParam("file") MultipartFile file,@RequestHeader("Authorization") String token) {
        try {
            String filename = fileStorageService.store(file);
            token = token.substring(7);
            String email = jwtService.extractUsername(token);
            if(userService.updateProfilePicture(email, filename)) {
                return ResponseEntity.ok(filename);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found");
            }
        } catch (StorageException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update profile picture: ");
        }
    }

    @GetMapping("/profile/picture")
    public ResponseEntity<Resource> loadProfilePicture(@RequestHeader("Authorization") String token) {
        token = token.substring(7);
        String email = jwtService.extractUsername(token);

        return userService.getProfilePicture(email);
    }

    @GetMapping("/becomeDriver")
    public ResponseEntity<?> becomeDriver(@RequestHeader("Authorization") String token) {
        token = token.substring(7);
        String email = jwtService.extractUsername(token);
        if(userService.becomeDriver(email)) {
            return ResponseEntity.ok().build();
        }else  {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    @GetMapping("/ride/{id}")
    public ResponseEntity<?> getRide(@RequestHeader("Authorization") String token, @PathVariable("id") long id) {
        token = token.substring(7);
        String email = jwtService.extractUsername(token);
        Optional<Ride> ride = rideRepository.findById(id);
        if(ride.isPresent()) {
            Ride rideRide = ride.get();
            UserInfo driver = rideRide.getDriver();
            if(ride.get().isApproved()){
                return ResponseEntity.ok(ride.get());
            }


            if(email.equals(driver.getEmail())) {
                return ResponseEntity.ok(ride.get());
            }
        }
        return ResponseEntity.notFound().build();

    }

    @GetMapping("/userProfile/picture")
    public ResponseEntity<Resource> getUserProfilePicture(@RequestParam("email") String email) {
        if(userService.findByEmail(email) != null) {
            return userService.getProfilePicture(email);
        }
        return ResponseEntity.notFound().build();

    }

    @GetMapping("/searchRides")
    public List<Ride> getRides(
            @RequestParam("deplat") double deplat,
            @RequestParam("deplon") double deplon,
            @RequestParam("destlat") double destlat,
            @RequestParam("destlon") double destlon

    ) {
        return rideRepository.findClosestRide(deplat,deplon,destlat,destlon);
    }

    @GetMapping("/car/photo")
    public ResponseEntity<Resource> getPhoto(@RequestParam String name,@RequestParam String email) {
        try {
            UserInfo user = userService.findByEmail(email);
            if(user != null) {
                if(user.getCar().getPhotos().contains(name)) {
                    Resource photo = fileStorageService.loadAsResource(name);
                    return ResponseEntity.ok(photo);
                }
            }
            throw new StorageException("");
        }catch (StorageException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/latestRide")
    public ResponseEntity<?> getLatestRide() {
        return ResponseEntity.ok(rideRepository.findRidesByApprovedOrderByIdDesc(PageRequest.of(0, 5)));
    }

    @PutMapping("/phoneNumber/{phoneNumber}")
    public ResponseEntity<?>  updatePhoneNumber(@RequestHeader("Authorization") String token, @PathVariable long phoneNumber) {
        token = token.substring(7);
        System.out.println(phoneNumber);
        if (Long.toString(phoneNumber).length() == 8){
            String email = jwtService.extractUsername(token);
            UserInfo user = userService.findByEmail(email);
            user.setNumber(phoneNumber);
            userInfoRepository.save(user);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }


    @ExceptionHandler
    public ResponseEntity<?> handle(Exception ex) {
        System.err.println(ex);
        return ResponseEntity.badRequest().build();


    }
}