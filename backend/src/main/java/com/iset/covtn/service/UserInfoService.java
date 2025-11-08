package com.iset.covtn.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.iset.covtn.exceptions.UserDejaExistException;
import com.iset.covtn.models.UserInfo;
import com.iset.covtn.repository.UserInfoRepository;


@Service
public class UserInfoService implements UserDetailsService {

    @Autowired
    private UserInfoRepository repository;

    private final ObjectProvider<PasswordEncoder> passwordEncoderProvider;

    @Autowired
    public UserInfoService(ObjectProvider<PasswordEncoder> passwordEncoderProvider) {
        this.passwordEncoderProvider = passwordEncoderProvider;
    }
    // Method to load user details by username (email)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch user from the database by email (username)
        Optional<UserInfo> userInfo = repository.findByEmail(username);
        
        if (userInfo.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        
        return userInfo.map(UserInfoDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // Add any additional methods for registering or managing users
    public String addUser(UserInfo userInfo) throws UserDejaExistException {
        // Check if user already exists
        if (repository.findByEmail(userInfo.getEmail()).isPresent()) {
            throw new UserDejaExistException("User already exists with email: " + userInfo.getEmail());
        }

        // Encrypt password before saving
        PasswordEncoder encoder = passwordEncoderProvider.getIfAvailable();
        userInfo.setPassword(encoder.encode(userInfo.getPassword())); 
        userInfo.addRole("ROLE_PASSENGER");
        repository.save(userInfo);
        return "User added successfully!";
    }
    /**
     * Rechercher un utilisateur par email
     */
    public UserInfo findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));
    }
    public String updateUser(String id, UserInfo updatedUserInfo) {
        Optional<UserInfo> existingUser = repository.findById(id);
                PasswordEncoder encoder = passwordEncoderProvider.getIfAvailable();

        if (existingUser.isPresent()) {
            UserInfo user = existingUser.get();
            user.setEmail(updatedUserInfo.getEmail());
            if (updatedUserInfo.getPassword() != null && !updatedUserInfo.getPassword().isEmpty()) {
                user.setPassword(encoder.encode(updatedUserInfo.getPassword()));
            }
            repository.save(user);
            return "User Updated Successfully";
        }
        return "User Not Found";
    }

    public String deleteUser(String email) {
        if (repository.existsById(email)) {
            repository.deleteById(email);
            return "User Deleted Successfully";
        }
        return "User Not Found";
    }

    public List<UserInfo> getAllUsers() {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }





    // ---------------------------------------------------------
    // 🔹 MÉTHODES SPÉCIFIQUES : DRIVERS
    // ---------------------------------------------------------

    public String addDriver(UserInfo driverInfo) {
                PasswordEncoder encoder = passwordEncoderProvider.getIfAvailable();

        driverInfo.addRole("ROLE_DRIVER");
        driverInfo.setPassword(encoder.encode(driverInfo.getPassword()));
        repository.save(driverInfo);
        return "Conducteur ajouté avec succès ✅";
    }

    public List<UserInfo> getAllDrivers() {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .filter(user -> "ROLE_DRIVER".equals(user.getRoles()))
                .collect(Collectors.toList());
    }

    public String updateDriver(String id, UserInfo driverInfo) {
                PasswordEncoder encoder = passwordEncoderProvider.getIfAvailable();

        Optional<UserInfo> existingDriver = repository.findById(id);
        if (existingDriver.isPresent()) {
            UserInfo driver = existingDriver.get();
            driver.setEmail(driverInfo.getEmail());
            if (driverInfo.getPassword() != null && !driverInfo.getPassword().isEmpty()) {
                driver.setPassword(encoder.encode(driverInfo.getPassword()));
            }
            driver.addRole("ROLE_DRIVER");
            repository.save(driver);
            return "Conducteur mis à jour avec succès ✅";
        }
        return "Conducteur introuvable ❌";
    }

    public String deleteDriver(String email) {
        if (repository.existsById(email)) {
            repository.deleteById(email);
            return "Conducteur supprimé avec succès ✅";
        }
        return "Conducteur introuvable ❌";
    }

    // ---------------------------------------------------------
    // 🔹 MÉTHODES SPÉCIFIQUES : PASSENGERS
    // ---------------------------------------------------------

    public String addPassenger(UserInfo passengerInfo) {
                PasswordEncoder encoder = passwordEncoderProvider.getIfAvailable();

        passengerInfo.addRole("ROLE_PASSENGER");
        passengerInfo.setPassword(encoder.encode(passengerInfo.getPassword()));
        repository.save(passengerInfo);
        return "Passager ajouté avec succès ✅";
    }

    public List<UserInfo> getAllPassengers() {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .filter(user -> "ROLE_PASSENGER".equals(user.getRoles()))
                .collect(Collectors.toList());
    }

    public String updatePassenger(String email, UserInfo passengerInfo) {
                PasswordEncoder encoder = passwordEncoderProvider.getIfAvailable();

        Optional<UserInfo> existingPassenger = repository.findById(email);
        if (existingPassenger.isPresent()) {
            UserInfo passenger = existingPassenger.get();
            passenger.setEmail(passengerInfo.getEmail());
            if (passengerInfo.getPassword() != null && !passengerInfo.getPassword().isEmpty()) {
                passenger.setPassword(encoder.encode(passengerInfo.getPassword()));
            }
            passenger.addRole("ROLE_PASSENGER");
            repository.save(passenger);
            return "Passager mis à jour avec succès ✅";

        }
        return "Passager introuvable ❌";
    }



    public String deletePassenger(String email) {
        if (repository.existsById(email)) {
            repository.deleteById(email);
            return "Passager supprimé avec succès ✅";
        }
        return "Passager introuvable ❌";
    }



    public void makeAdmin(String email) {
        Optional<UserInfo> userOpt = repository.findById(email);
        if (userOpt.isPresent()) {
            UserInfo user = userOpt.get();
            user.addRole("ROLE_ADMIN");
            repository.save(user);
        } else {
            throw new UsernameNotFoundException("Utilisateur introuvable : " + email);
        }
    }
}

    
    





