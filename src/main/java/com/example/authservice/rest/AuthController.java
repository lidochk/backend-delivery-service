package com.example.authservice.rest;

import com.example.authservice.domain.UserCredential;
import com.example.authservice.dto.AuthRequest;
import com.example.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getName(),authRequest.getPassword()));
        if (authentication.isAuthenticated()){
            return authService.generateToken(authRequest.getName());
        }
        else {
            throw new RuntimeException("invalid access");
        }
    }

    @GetMapping("/validate")
    public String validate(@RequestParam String token){
        authService.validateToken(token);
        return "Token is valid";
    }

}