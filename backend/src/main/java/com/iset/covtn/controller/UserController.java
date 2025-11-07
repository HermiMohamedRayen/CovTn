package com.iset.covtn.controller;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iset.covtn.exceptions.UserDejaExistException;
import com.iset.covtn.models.AuthObj;
import com.iset.covtn.models.AuthRequest;
import com.iset.covtn.models.UserInfo;
import com.iset.covtn.repository.UserInfoRepository;
import com.iset.covtn.service.JwtService;
import com.iset.covtn.service.UserInfoService;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/auth")
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

    private HashMap<String, AuthObj> emailsCodes = new HashMap<>();

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
        } catch (UserDejaExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body( e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la création de l'utilisateur : " + e.getMessage());
        }
    }

    @PostMapping("/validateMail")
    public ResponseEntity<?> validateMail(@RequestBody AuthObj authObj) {

        if(emailsCodes.get(authObj.getEmail()) != null && emailsCodes.get(authObj.getEmail()).equals(authObj)){
            String token = emailsCodes.get(authObj.getEmail()).getToken();
            emailsCodes.remove(authObj.getEmail());
            return ResponseEntity.ok(token);
        }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Code invalide.");
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
                AuthObj authObj = new AuthObj(authRequest.getUsername(), String.valueOf(new Date().getTime()), "","");
                int randomNumber = new Random().nextInt(9000) + 1000;
                String token = jwtService.generateToken((UserDetails) authentication.getPrincipal());
                AuthObj authObjstore = new AuthObj(authRequest.getUsername(), authObj.getId(), String.valueOf(randomNumber),token);
                emailsCodes.put(authRequest.getUsername(), authObjstore);
                System.out.println("Code for " + authRequest.getUsername() + ": " + randomNumber + " .. "+ authObjstore.getId());
                //TODO: send email with code

                return ResponseEntity.ok(authObj);
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
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable String id) {
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
    public ResponseEntity<String> updateDriver(@PathVariable String id, @RequestBody UserInfo driverInfo) {
        return ResponseEntity.ok(userService.updateDriver(id, driverInfo));
    }

    @DeleteMapping("/drivers/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deleteDriver(@PathVariable String id) {
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
    public ResponseEntity<String> updatePassenger(@PathVariable String id, @RequestBody UserInfo passengerInfo) {
        return ResponseEntity.ok(userService.updatePassenger(id, passengerInfo));
    }

    @DeleteMapping("/passengers/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deletePassenger(@PathVariable String id) {
        return ResponseEntity.ok(userService.deletePassenger(id));
    }
}
