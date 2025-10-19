package controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import models.AuthRequest;
import models.UserInfo;
import service.JwtService;
import service.UserInfoService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    private UserInfoService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Page d'accueil publique
     */
    @GetMapping("/welcome")
    public String welcome() {
        return "🚗 Bienvenue sur CovTn - Plateforme de covoiturage sécurisée avec JWT";
    }

    /**
     * Ajouter un nouvel utilisateur (passager, conducteur ou admin)
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserInfo userInfo) {
        try {
            String result = userService.addUser(userInfo);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la création de l'utilisateur : " + e.getMessage());
        }
    }

    /**
     * Authentification + génération de token JWT
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()));

            if (authentication.isAuthenticated()) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String token = jwtService.generateToken(userDetails);
                return ResponseEntity.ok(token);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Échec d'authentification.");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants incorrects.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur d'authentification : " + e.getMessage());
        }
    }

    /**
     * Récupérer les infos de l'utilisateur connecté
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser(Authentication authentication) {
        UserInfo user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(user);
    }

    /**
     * Liste de tous les utilisateurs (admin uniquement)
     */
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UserInfo>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Supprimer un utilisateur (admin uniquement)
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        try {
            String result = userService.deleteUser(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur introuvable avec l'id : " + id);
        }
    }

    /**
     * CRUD pour les conducteurs (Driver)
     */
    @PostMapping("/drivers")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> addDriver(@RequestBody UserInfo driverInfo) {
        return ResponseEntity.ok(userService.addDriver(driverInfo));
    }

    @GetMapping("/drivers")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DRIVER')")
    public ResponseEntity<List<UserInfo>> getAllDrivers() {
        return ResponseEntity.ok(userService.getAllDrivers());
    }

    @PutMapping("/drivers/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> updateDriver(@PathVariable Integer id, @RequestBody UserInfo driverInfo) {
        return ResponseEntity.ok(userService.updateDriver(id, driverInfo));
    }

    @DeleteMapping("/drivers/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deleteDriver(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.deleteDriver(id));
    }

    /**
     * CRUD pour les passagers (Passenger)
     */
    @PostMapping("/passengers")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> addPassenger(@RequestBody UserInfo passengerInfo) {
        return ResponseEntity.ok(userService.addPassenger(passengerInfo));
    }

    @GetMapping("/passengers")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PASSENGER')")
    public ResponseEntity<List<UserInfo>> getAllPassengers() {
        return ResponseEntity.ok(userService.getAllPassengers());
    }

    @PutMapping("/passengers/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> updatePassenger(@PathVariable Integer id, @RequestBody UserInfo passengerInfo) {
        return ResponseEntity.ok(userService.updatePassenger(id, passengerInfo));
    }

    @DeleteMapping("/passengers/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deletePassenger(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.deletePassenger(id));
    }
}
