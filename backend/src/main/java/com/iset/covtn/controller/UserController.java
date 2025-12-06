package com.iset.covtn.controller;

import com.iset.covtn.models.Rating;
import com.iset.covtn.models.Ride;
import com.iset.covtn.models.RideParticipation;
import com.iset.covtn.models.UserInfo;
import com.iset.covtn.repository.RatingRepository;
import com.iset.covtn.repository.RideParticipationRepository;
import com.iset.covtn.repository.RideRepository;
import com.iset.covtn.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.iset.covtn.exceptions.StorageException;
import com.iset.covtn.service.FileSystemStorageService;
import com.iset.covtn.service.JwtService;
import com.iset.covtn.service.UserInfoService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


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
    @Autowired
    private RideParticipationRepository rideParticipationRepository;
    @Autowired
    private RatingRepository ratingRepository;


    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

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
    public ResponseEntity<?> getRide( @PathVariable("id") long id) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Ride> ride = rideRepository.findById(id);
        UserInfo user = userService.findByEmail(userDetails.getUsername());
        if(ride.isPresent()) {
            Ride rideRide = ride.get();
            UserInfo driver = rideRide.getDriver();
            if(ride.get().isApproved()){
                return ResponseEntity.ok(ride.get());
            }

            for(String s : user.getRoles()){
                if(s.equals("ROLE_ADMIN")){
                    return ResponseEntity.ok(ride.get());
                }
            }

            if(userDetails.getUsername().equals(driver.getEmail())) {
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
            @RequestParam double deplat,
            @RequestParam double deplon,
            @RequestParam double destlat,
            @RequestParam double destlon,
            @RequestParam String depTime,
            @RequestParam String arrTime


    )  throws ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date depTimef = sdf.parse(depTime);
        Date arrTimef = sdf.parse(arrTime);
        return rideRepository.findClosestRide(deplat,deplon,destlat,destlon,depTimef,arrTimef);
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


    @GetMapping("/latestRide/{deplat}/{deplon}")
    public ResponseEntity<?> getLatestRideToUser(
            @PathVariable double deplat,
            @PathVariable double deplon
    ) {
        return ResponseEntity.ok(rideRepository.findApprovedLatestRideToUser(deplat,deplon,PageRequest.of(0, 5)));
    }

    @PutMapping("/phoneNumber/{phoneNumber}")
    public ResponseEntity<?>  updatePhoneNumber(@RequestHeader("Authorization") String token, @PathVariable long phoneNumber) {
        token = token.substring(7);
        System.out.println(phoneNumber);
        if (Long.toString(phoneNumber).length() == 8){
            String email = jwtService.extractUsername(token);
            UserInfo user = userService.findByEmail(email);
            user.setNumber(Long.toString(phoneNumber));
            userInfoRepository.save(user);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/ride/participate/{id}")
    public ResponseEntity<?> participateInRide(@PathVariable long id) {

        Optional<Ride> ride = rideRepository.findById(id);
        if(!ride.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        if(!ride.get().isApproved()) {
            return  ResponseEntity.badRequest().build();
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        UserInfo user = userService.findByEmail(userDetails.getUsername());
        if(user.getRides().contains(ride.get())) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
        if(ride.get().getDepartureTime().before(new Date())){
            return ResponseEntity.status(HttpStatus.GONE).build();
        }
        if(ride.get().getRideParticipations().size() >= ride.get().getDriver().getCar().getSeats()){
            return ResponseEntity.status(HttpStatus.IM_USED).build();
        }
        for(RideParticipation participation : ride.get().getRideParticipations()){
            if(participation.getRider().getEmail().equals(user.getEmail())){
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }
        String dep = "";
        String arr = "";
        try{
            dep = " that depart at "+sdf.format(ride.get().getDepartureTime());
            arr = " and arrive at "+sdf.format(ride.get().getArrivalTime());
        }catch (Exception e){
            System.err.println(e);
        }
        userService.sendMessage(ride.get().getDriver().getEmail(),user.getFirstName()+" "+user.getLastName()+" has booked one of your rides"+dep+arr);
        RideParticipation rideParticipation = new RideParticipation();
        rideParticipation.setRide(ride.get());
        rideParticipation.setRider(user);
        rideParticipationRepository.save(rideParticipation);
        return ResponseEntity.ok().build();

    }

    /*
    @GetMapping("/ride/{id}/participations")
    public ResponseEntity<?> getParticipation(@PathVariable long id) {
        Optional<Ride>  ride = rideRepository.findById(id);
        if(!ride.isPresent()) {
            return ResponseEntity.notFound().build();
        }else {
            return ResponseEntity.ok(ride.get().getRideParticipations());
        }
    }

     */

    @GetMapping("/ride/{id}/isParticipated")
    public ResponseEntity<?> getIsParticipated(@PathVariable long id) {
        Optional<Ride> ride = rideRepository.findById(id);
        if(!ride.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserInfo user = userService.findByEmail(userDetails.getUsername());
        for (RideParticipation rideParticipation : ride.get().getRideParticipations()) {
            if(rideParticipation.getRider().getEmail().equals(user.getEmail())) {
                return ResponseEntity.ok("{\"participated\" : true}");
            }
        }
        return ResponseEntity.ok("{\"participated\" : false}");
    }

    @GetMapping("/participations")
    public ResponseEntity<?> getMyParticipations() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserInfo user = userService.findByEmail(userDetails.getUsername());
        user.getRideParticipations().forEach(rideParticipation -> {
           rideParticipation.getRide().getRideParticipations().clear();
        });
        return ResponseEntity.ok(user.getRideParticipations());
    }

    @PostMapping("/comment")
    public ResponseEntity<?> addComment(@RequestBody Rating comment) {
        System.err.println("begin");
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserInfo user = userService.findByEmail(userDetails.getUsername());
        Optional<UserInfo> targetUser = userInfoRepository.findByEmail(comment.getTargetUser().getEmail());
        if(!targetUser.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        if(targetUser.get().getEmail().equals(user.getEmail())) {
            return ResponseEntity.badRequest().build();
        }
        comment.setId(null);
        comment.setUser(user);
        comment.setTargetUser(targetUser.get());
        ratingRepository.save(comment);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getCarInfo/{email}")
    public ResponseEntity<?> getCarInfo(@PathVariable String email) {
        UserInfo user = userService.findByEmail(email);
        if(user==null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user.getCar());
    }

    @GetMapping("/userInfo/{email}")
    public ResponseEntity<?> getUserInfo(@PathVariable String email) {
        UserInfo user = userService.findByEmail(email);
        if(user==null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/ride/unparticipate/{particip_id}")
    public ResponseEntity<?> unParticipate(@PathVariable("particip_id") long id) {
        Optional<RideParticipation> part = rideParticipationRepository.findById(id);
        if(!part.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserInfo user = userService.findByEmail(userDetails.getUsername());
        if(!part.get().getRider().getEmail().equals(user.getEmail())) {
            return ResponseEntity.badRequest().build();
        }
        if(part.get().getRide().getDepartureTime().before(new Date())) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }
        rideParticipationRepository.delete(part.get());
        return ResponseEntity.ok().build();

    }


    @DeleteMapping("/ride/unparticipate/r/{ride_id}")
    public ResponseEntity<?> unParticipateByRideId(@PathVariable("ride_id") long id) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserInfo user = userService.findByEmail(userDetails.getUsername());
        Optional<RideParticipation> part = rideParticipationRepository.findByRideIdAndRiderEmail(id, user.getEmail());
        if(!part.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        if(part.get().getRide().getDepartureTime().before(new Date())) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }
        rideParticipationRepository.delete(part.get());
        return ResponseEntity.ok().build();

    }


    @ExceptionHandler
    public ResponseEntity<?> handle(Exception ex) {
        System.err.println(ex);
        return ResponseEntity.badRequest().build();

    }
}