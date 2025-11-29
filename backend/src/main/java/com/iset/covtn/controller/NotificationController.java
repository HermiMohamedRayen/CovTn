package com.iset.covtn.controller;

import com.iset.covtn.models.UserInfo;
import com.iset.covtn.service.JwtService;
import com.iset.covtn.service.UserInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final JwtService jwtService;
    private final UserInfoService userInfoService;

    public NotificationController(JwtService jwtService, UserInfoService userInfoService) {
        this.jwtService = jwtService;
        this.userInfoService = userInfoService;
    }

    @GetMapping("/stream")
    public SseEmitter stream(@RequestParam("token") String token) {
        String email ;
        jwtService.extractUsername(token);
        try {
             email = jwtService.extractUsername(token);
        } catch (Exception e) {
            System.err.println(e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        return userInfoService.getMessages(email);
    }

}
