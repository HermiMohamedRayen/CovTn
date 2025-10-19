package service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import models.UserInfo;
import repository.UserInfoRepository;

@Service
public class UserInfoService implements UserDetailsService {

    private final UserInfoRepository repository;
    private final PasswordEncoder encoder;

    @Autowired
    public UserInfoService(UserInfoRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    /**
     * Méthode utilisée par Spring Security pour charger un utilisateur via son email (username).
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserInfo> userInfo = repository.findByEmail(username);
        if (userInfo.isEmpty()) {
            throw new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + username);
        }
        return new UserInfoDetails(userInfo.get());
    }

    /**
     * Enregistrer un nouvel utilisateur (avec mot de passe encodé)
     */
    public String addUser(UserInfo userInfo) {
        if (repository.findByEmail(userInfo.getEmail()).isPresent()) {
            return "Cet email est déjà utilisé !";
        }
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));
        repository.save(userInfo);
        return "Utilisateur ajouté avec succès ✅";
    }

    /**
     * Rechercher un utilisateur par email
     */
    public UserInfo findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));
    }
    public String updateUser(Integer id, UserInfo updatedUserInfo) {
        Optional<UserInfo> existingUser = repository.findById(id);
        if (existingUser.isPresent()) {
            UserInfo user = existingUser.get();
            user.setName(updatedUserInfo.getName());
            user.setEmail(updatedUserInfo.getEmail());
            if (updatedUserInfo.getPassword() != null && !updatedUserInfo.getPassword().isEmpty()) {
                user.setPassword(encoder.encode(updatedUserInfo.getPassword()));
            }
            repository.save(user);
            return "User Updated Successfully";
        }
        return "User Not Found";
    }

    public String deleteUser(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
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
        driverInfo.setRoles("ROLE_DRIVER");
        driverInfo.setPassword(encoder.encode(driverInfo.getPassword()));
        repository.save(driverInfo);
        return "Conducteur ajouté avec succès ✅";
    }

    public List<UserInfo> getAllDrivers() {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .filter(user -> "ROLE_DRIVER".equals(user.getRoles()))
                .collect(Collectors.toList());
    }

    public String updateDriver(Integer id, UserInfo driverInfo) {
        Optional<UserInfo> existingDriver = repository.findById(id);
        if (existingDriver.isPresent()) {
            UserInfo driver = existingDriver.get();
            driver.setName(driverInfo.getName());
            driver.setEmail(driverInfo.getEmail());
            if (driverInfo.getPassword() != null && !driverInfo.getPassword().isEmpty()) {
                driver.setPassword(encoder.encode(driverInfo.getPassword()));
            }
            driver.setRoles("ROLE_DRIVER");
            repository.save(driver);
            return "Conducteur mis à jour avec succès ✅";
        }
        return "Conducteur introuvable ❌";
    }

    public String deleteDriver(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return "Conducteur supprimé avec succès ✅";
        }
        return "Conducteur introuvable ❌";
    }

    // ---------------------------------------------------------
    // 🔹 MÉTHODES SPÉCIFIQUES : PASSENGERS
    // ---------------------------------------------------------

    public String addPassenger(UserInfo passengerInfo) {
        passengerInfo.setRoles("ROLE_PASSENGER");
        passengerInfo.setPassword(encoder.encode(passengerInfo.getPassword()));
        repository.save(passengerInfo);
        return "Passager ajouté avec succès ✅";
    }

    public List<UserInfo> getAllPassengers() {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .filter(user -> "ROLE_PASSENGER".equals(user.getRoles()))
                .collect(Collectors.toList());
    }

    public String updatePassenger(Integer id, UserInfo passengerInfo) {
        Optional<UserInfo> existingPassenger = repository.findById(id);
        if (existingPassenger.isPresent()) {
            UserInfo passenger = existingPassenger.get();
            passenger.setName(passengerInfo.getName());
            passenger.setEmail(passengerInfo.getEmail());
            if (passengerInfo.getPassword() != null && !passengerInfo.getPassword().isEmpty()) {
                passenger.setPassword(encoder.encode(passengerInfo.getPassword()));
            }
            passenger.setRoles("ROLE_PASSENGER");
            repository.save(passenger);
            return "Passager mis à jour avec succès ✅";
        }
        return "Passager introuvable ❌";
    }

    public String deletePassenger(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return "Passager supprimé avec succès ✅";
        }
        return "Passager introuvable ❌";
    }
}

    
    





