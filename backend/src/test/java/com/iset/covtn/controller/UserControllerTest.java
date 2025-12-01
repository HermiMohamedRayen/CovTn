package com.iset.covtn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iset.covtn.models.Car;
import com.iset.covtn.models.Rating;
import com.iset.covtn.models.Ride;
import com.iset.covtn.models.RideParticipation;
import com.iset.covtn.models.UserInfo;
import com.iset.covtn.repository.RatingRepository;
import com.iset.covtn.repository.RideParticipationRepository;
import com.iset.covtn.repository.RideRepository;
import com.iset.covtn.repository.UserInfoRepository;
import com.iset.covtn.service.FileSystemStorageService;
import com.iset.covtn.service.JwtService;
import com.iset.covtn.service.UserInfoService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserControllerTest.MockitoConfig.class)
class UserControllerTest {

    @TestConfiguration
    static class MockitoConfig {
        @Bean FileSystemStorageService fileSystemStorageService() { return Mockito.mock(FileSystemStorageService.class); }
        @Bean JwtService jwtService() { return Mockito.mock(JwtService.class); }
        @Bean UserInfoService userInfoService() { return Mockito.mock(UserInfoService.class); }
        @Bean RideRepository rideRepository() { return Mockito.mock(RideRepository.class); }
        @Bean UserInfoRepository userInfoRepository() { return Mockito.mock(UserInfoRepository.class); }
        @Bean RideParticipationRepository rideParticipationRepository() { return Mockito.mock(RideParticipationRepository.class); }
        @Bean RatingRepository ratingRepository() { return Mockito.mock(RatingRepository.class); }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private FileSystemStorageService fileStorageService;
    @Autowired private JwtService jwtService;
    @Autowired private UserInfoService userInfoService;
    @Autowired private RideRepository rideRepository;
    @Autowired private UserInfoRepository userInfoRepository;
    @Autowired private RideParticipationRepository rideParticipationRepository;
    @Autowired private RatingRepository ratingRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private UserInfo testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserInfo();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRoles(new HashSet<>(Collections.singletonList("ROLE_USER")));
        testUser.setRides(new ArrayList<>());
        testUser.setRideParticipations(new ArrayList<>());

        when(jwtService.extractUsername(anyString())).thenReturn(testUser.getEmail());
        when(userInfoService.findByEmail(testUser.getEmail())).thenReturn(testUser);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateProfilePicture_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image".getBytes());
        when(fileStorageService.store(file)).thenReturn("new_picture.jpg");
        when(userInfoService.updateProfilePicture(testUser.getEmail(), "new_picture.jpg")).thenReturn(true);

        mockMvc.perform(multipart("/api/user/profile/updatePicture")
                        .file(file)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void loadProfilePicture_success() throws Exception {
        Resource resource = new ByteArrayResource("picture data".getBytes());
        when(userInfoService.getProfilePicture(testUser.getEmail())).thenReturn(org.springframework.http.ResponseEntity.ok(resource));

        mockMvc.perform(get("/api/user/profile/picture")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void becomeDriver_success() throws Exception {
        when(userInfoService.becomeDriver(testUser.getEmail())).thenReturn(true);

        mockMvc.perform(get("/api/user/becomeDriver")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getRide_success() throws Exception {
        Ride ride = new Ride();
        ride.setId(1L);
        ride.setApproved(true);
        UserInfo driver = new UserInfo();
        driver.setEmail("driver@example.com");
        driver.setRoles(new HashSet<>(Collections.singletonList("driver")));
        ride.setDriver(driver);

        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));

        mockMvc.perform(get("/api/user/ride/1")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void participateInRide_success() throws Exception {
        Ride ride = new Ride();
        ride.setId(1L);
        ride.setApproved(true);
        ride.setDepartureTime(new Date(System.currentTimeMillis() + 100000));
        Car car = new Car();
        car.setSeats(4);
        UserInfo driver = new UserInfo();
        driver.setCar(car);
        ride.setDriver(driver);
        ride.setRideParticipations(Collections.emptyList());
        driver.setRides(Collections.singletonList(ride));

        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(rideParticipationRepository.save(any(RideParticipation.class))).thenReturn(new RideParticipation());

        mockMvc.perform(post("/api/user/ride/participate/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addComment_success() throws Exception {
        UserInfo targetUser = new UserInfo();
        targetUser.setEmail("target@example.com");
        targetUser.setRoles(new HashSet<>(Collections.singletonList("ROLE_USER")));
        Rating rating = new RatingExt();
        rating.setTargetUser(targetUser);
        rating.setComment("Great driver!");
        rating.setRating(5);


        System.err.println(objectMapper.writeValueAsString(rating));


        when(userInfoRepository.findByEmail("target@example.com")).thenReturn(Optional.of(targetUser));
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

        mockMvc.perform(post("/api/user/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rating)))
                .andExpect(status().isOk());
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class RatingExt extends  Rating {
    private UserInfo targetUser;
}

