package com.iset.covtn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.iset.covtn.exceptions.StorageException;
import com.iset.covtn.service.FileSystemStorageService;
import com.iset.covtn.service.JwtService;
import com.iset.covtn.service.UserInfoService;



@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private FileSystemStorageService fileStorageService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserInfoService userService;
    
    @PostMapping("/profile/updatePicture")
    public ResponseEntity<String> updateProfilePicture(@RequestParam("file") MultipartFile file,@RequestHeader("Authorization") String token) {
        try {
            String filename = fileStorageService.store(file);
            token = token.substring(7);
            String email = jwtService.extractUsername(token);
            if(userService.updateProfilePicture(email, filename)) {
                return ResponseEntity.ok("Profile picture updated successfully: " + filename);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found");
            }
        } catch (StorageException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update profile picture: " + e.getMessage());
        }
    }
}