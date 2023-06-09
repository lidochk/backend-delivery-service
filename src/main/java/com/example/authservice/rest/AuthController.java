package com.example.authservice.rest;

import com.example.authservice.config.CustomUserDetails;
import com.example.authservice.entity.UserCredential;
import com.example.authservice.dto.AuthRequest;
import com.example.authservice.service.AuthService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {


    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public String addNewUser(@RequestBody UserCredential userCredential){
        return authService.saveUser(userCredential);
    }

    @PostMapping("/token")
    public String getToken(@RequestBody AuthRequest authRequest){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getName(), authRequest.getPassword())
        );
        if (authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return authService.generateToken(authRequest.getName(), userDetails.getRole());
        } else {
            throw new RuntimeException("invalid access");
        }

    }

    @GetMapping("/validate")
    public String validate(@RequestParam String token){
        authService.validateToken(token);
        return "Token is valid";
    }

    @PutMapping("/updateUserInfo")
    public void update(@RequestBody UserCredential userCredential){
        authService.updateUser(userCredential);
    }

    @PostMapping("/login")
    public Map<String, String> loginUser(@RequestBody UserCredential userCredential){
        return authService.loginUser(userCredential);
    }

}
