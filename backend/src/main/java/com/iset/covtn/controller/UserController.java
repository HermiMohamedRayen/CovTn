package com.iset.covtn.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iset.covtn.models.AuthRequest;
import com.iset.covtn.models.UserInfo;
import com.iset.covtn.repository.UserInfoRepository;
import com.iset.covtn.service.JwtService;
import com.iset.covtn.service.UserInfoService;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserInfoRepository userInfoRepository;

    @Autowired
    private UserInfoService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private HttpServletRequest request;

    UserController(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

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
    @PreAuthorize("hasAuthority('ROLE_PASSENGER')")
    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser() {
        String token = request.getHeader("Authorization").substring(7);
        UserInfo user = userService.findByEmail(jwtService.extractUsername(token));
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
    @DeleteMapping("/users/{email}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable String email) {
        try {
            String result = userService.deleteUser(email);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur introuvable avec l'email : " + email);
        }
    }
    @PutMapping("/Updateusers/{email}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> updateUser(@PathVariable String email, @RequestBody UserInfo userInfo) {
        return ResponseEntity.ok(userService.updateUser(email, userInfo));
    }
    @GetMapping("/users/{email}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserInfo> getUserByEmail(@PathVariable String email) {
        UserInfo user = userService.findByEmail(email);
        return ResponseEntity.ok(user);
    }
@PostMapping("/addUser")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> addUser(@RequestBody UserInfo userInfo) {
        return ResponseEntity.ok(userService.addUser(userInfo));
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
    public ResponseEntity<String> updateDriver(@PathVariable String email, @RequestBody UserInfo driverInfo) {
        return ResponseEntity.ok(userService.updateDriver(email, driverInfo));
    }

    @DeleteMapping("/drivers/{email}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deleteDriver(@PathVariable String email) {
        return ResponseEntity.ok(userService.deleteDriver(email));
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

    @PutMapping("/passengers/{email}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> updatePassenger(@PathVariable String email, @RequestBody UserInfo passengerInfo) {
        return ResponseEntity.ok(userService.updatePassenger(email, passengerInfo));
    }

    @DeleteMapping("/passengers/{email}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deletePassenger(@PathVariable String email) {
        return ResponseEntity.ok(userService.deletePassenger(email));
    }
}
