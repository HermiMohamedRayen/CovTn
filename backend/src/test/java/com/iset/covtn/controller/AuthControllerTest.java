package com.iset.covtn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iset.covtn.exceptions.UserDejaExistException;
import com.iset.covtn.models.AuthObj;
import com.iset.covtn.models.AuthRequest;
import com.iset.covtn.models.EmailDetails;
import com.iset.covtn.models.UserInfo;
import com.iset.covtn.repository.UserInfoRepository;
import com.iset.covtn.service.EmailServiceImpl;
import com.iset.covtn.service.JwtService;
import com.iset.covtn.service.UserInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AuthControllerTest.MockitoConfig.class)
class AuthControllerTest {

    @TestConfiguration
    static class MockitoConfig {
        @Bean UserInfoService userInfoService() { return Mockito.mock(UserInfoService.class); }
        @Bean JwtService jwtService() { return Mockito.mock(JwtService.class); }
        @Bean AuthenticationManager authenticationManager() { return Mockito.mock(AuthenticationManager.class); }
        @Bean EmailServiceImpl emailServiceImpl() { return Mockito.mock(EmailServiceImpl.class); }
        @Bean UserInfoRepository userInfoRepository() { return Mockito.mock(UserInfoRepository.class); }
        @Bean JavaMailSender javaMailSender() { return Mockito.mock(JavaMailSender.class); }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private AuthController authController;
    @Autowired private UserInfoService userService;
    @Autowired private JwtService jwtService;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private EmailServiceImpl emailService;
    @Autowired private UserInfoRepository userInfoRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        when(emailService.sendSimpleMail(any(EmailDetails.class))).thenReturn("Mail Sent Successfully...");
    }



    @Test
    void registerUser_success_returnsAuthObj() throws Exception {
        UserInfo u = new UserInfo();
        u.setEmail("test@example.com");
        u.setPassword("pass12345");
        u.setRoles(new HashSet<>());

        when(userService.addUser(any(UserInfo.class))).thenReturn("User added successfully!");
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("generated-token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    void registerUser_conflict_returns409() throws Exception {
        UserInfo u = new UserInfo();
        u.setEmail("exist@example.com");
        u.setPassword("pass12345");
        u.setRoles(new HashSet<>());


        when(userService.addUser(any(UserInfo.class))).thenThrow(UserDejaExistException.class);


        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u)))
                .andExpect(status().isConflict());
    }


    @Test
    void validateMail_success_returnsToken() throws Exception {
        String email = "val@example.com";
        String id = "id-1234";
        String code = "9999";
        String token = "the-token";

        Field f = AuthController.class.getDeclaredField("emailsCodes");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        HashMap<String, AuthObj> map = (HashMap<String, AuthObj>) f.get(authController);
        map.put(email, new AuthObj(email, id, code, token));

        AuthObj payload = new AuthObj(email, id, code, null);

        mockMvc.perform(post("/api/auth/validateMail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string(token));
    }

    @Test
    void validateMail_invalidCode_returns401() throws Exception {
        AuthObj payload = new AuthObj("nope@example.com", "idx", "0000", null);
        mockMvc.perform(post("/api/auth/validateMail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Code invalide."));
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setUsername("u");
        req.setPassword("wrong");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Identifiants incorrects."));
    }

    @Test
    void login_success_returnsAuthObj() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setUsername("user@example.com");
        req.setPassword("pass12345");

        Authentication auth = Mockito.mock(Authentication.class);
        UserDetails principal = Mockito.mock(UserDetails.class);
        when(principal.getUsername()).thenReturn("user@example.com");
        when(auth.getPrincipal()).thenReturn(principal);
        when(auth.isAuthenticated()).thenReturn(true);

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    void refreshToken_success_returnsNewToken() throws Exception {
        when(jwtService.refreshToken(eq("oldtoken"))).thenReturn("newtoken");

        mockMvc.perform(get("/api/auth/refreshToken")
                        .header("Authorization", "Bearer oldtoken"))
                .andExpect(status().isOk())
                .andExpect(content().string("newtoken"));
    }

    @Test
    void getCurrentUser_returnsUserInfo() throws Exception {
        String token = "tok-123";
        String email = "me@example.com";


        when(jwtService.extractUsername(token)).thenReturn(email);

        UserInfo user = new UserInfo();
        user.setEmail(email);
        user.setPassword("x");
        user.setRoles(new HashSet<>());
        when(userService.findByEmail(email)).thenReturn(user);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

}
