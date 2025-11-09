package com.iset.covtn.controller;


import java.util.Date;
import java.util.HashMap;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iset.covtn.exceptions.UserDejaExistException;
import com.iset.covtn.models.AuthObj;
import com.iset.covtn.models.AuthRequest;
import com.iset.covtn.models.EmailDetails;
import com.iset.covtn.models.UserInfo;
import com.iset.covtn.service.EmailServiceImpl;
import com.iset.covtn.service.JwtService;
import com.iset.covtn.service.UserInfoDetails;
import com.iset.covtn.service.UserInfoService;

import jakarta.servlet.http.HttpServletRequest;



@RestController
@RequestMapping("/api/auth")
public class AuthController {


    @Autowired
    private UserInfoService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private EmailServiceImpl emailService;


    private final HashMap<String, AuthObj> emailsCodes = new HashMap<>();

    /**
     * Page d'accueil publique
     */
    @GetMapping("/welcome")
    public String welcome() {
        return "🚗 Bienvenue sur CovTn - Plateforme de covoiturage sécurisée avec JWT";
    }

    private AuthObj mailVerifier(String email,String token){
        AuthObj authObj = new AuthObj(email, String.valueOf(new Date().getTime()), "","");
                int randomNumber = new Random().nextInt(9000) + 1000;
                AuthObj authObjstore = new AuthObj(email, authObj.getId(), String.valueOf(randomNumber),token);
                emailsCodes.put(email, authObjstore);
                System.out.println("Code for " + email + ": " + randomNumber );
                emailService.sendSimpleMail(
                    new EmailDetails(
                        email,
                        "Votre code de vérification est : " + randomNumber,
                        "Code de vérification CovTn",
                        ""
                    )
                );
        return authObj;

    }

    /**
     * Ajouter un nouvel utilisateur (passager, conducteur ou admin)
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserInfo userInfo) {
        try {
            userService.addUser(userInfo);
            
            String token = jwtService.generateToken(new UserInfoDetails(userInfo));
            AuthObj result = mailVerifier(userInfo.getEmail(), token);
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
                AuthObj authObj = mailVerifier(authRequest.getUsername(),
                    jwtService.generateToken((UserDetails) authentication.getPrincipal()));
                
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

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DRIVER', 'ROLE_PASSENGER')")
    @GetMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        token = token.substring(7); 
        try {
            String newToken = jwtService.refreshToken(token);
            return ResponseEntity.ok(newToken);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Erreur lors du rafraîchissement du token : " + e.getMessage());
        }
    }

    /**
     * Récupérer les infos de l'utilisateur connecté
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser() {
        String token = request.getHeader("Authorization").substring(7);
        UserInfo user = userService.findByEmail(jwtService.extractUsername(token));
        return ResponseEntity.ok(user);
    }



    @GetMapping("/makeAdmin")
    public String makeAdmin(@RequestParam String email) {
         userService.makeAdmin(email);
        return "Utilisateur promu en tant qu'administrateur : " + email;
    }

   
    
}
