package com.example.authservice.service;

import com.example.authservice.Exception.ApiRequestException;
import com.example.authservice.dto.CourierDTO;
import com.example.authservice.dto.RestaurantDTO;
import com.example.authservice.entity.Role;
import com.example.authservice.entity.UserCredential;
import com.example.authservice.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AuthService {

    @Autowired
    private UserCredentialRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    RestTemplate restTemplate;

    public String saveUser(UserCredential credential){
        credential.setPassword(passwordEncoder.encode(credential.getPassword()));
        boolean email = repository.existsByEmail(credential.getEmail());
        if (email){
            throw new ApiRequestException("User with this email "+credential.getEmail()+ " already exist");
        }
        UserCredential savedUser = repository.save(credential);
        String token = generateToken(savedUser.getEmail(), savedUser.getRole());
        if (savedUser.getRole() == Role.OWNER){
            RestaurantDTO restaurantDTO = new RestaurantDTO();
            restaurantDTO.setRestaurantName(savedUser.getName());
            restaurantDTO.setIsActive(false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            HttpEntity<RestaurantDTO> requestEntity = new HttpEntity<>(restaurantDTO, headers);

            String uri = "http://localhost:8080/restaurant/create";
            ResponseEntity<Long> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, Long.class);
            Long idRestaurant = responseEntity.getBody();
            savedUser.setRestaurantId(idRestaurant);
            repository.save(savedUser); // Сохраняем пользователя снова с обновленным idRestaurant
        }
        //TODO:понять что делать
        if (savedUser.getRole() == Role.COURIER){
            CourierDTO courierDTO = new CourierDTO();
            courierDTO.setName(savedUser.getName());
            courierDTO.setEmail(savedUser.getEmail());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            HttpEntity<CourierDTO> requestEntity = new HttpEntity<>(courierDTO, headers);
            String uri = "http://localhost:8080/courier/registerCourier";
            restTemplate.exchange(uri, HttpMethod.POST, requestEntity, Long.class);
        }
        return "user added to the system";
    }


    public void updateUser(UserCredential updatedUser) {
        Optional<UserCredential> userCredentialOptional = repository.findByEmail(updatedUser.getEmail());

        if (userCredentialOptional.isPresent()) {
            UserCredential existingUser = userCredentialOptional.get();

            existingUser.setName(updatedUser.getName());
            existingUser.setEmail(updatedUser.getEmail());
            if (updatedUser.getPassword() != null) {
                existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }
            existingUser.setRole(updatedUser.getRole());

            repository.save(existingUser);
        } else {
            throw new ApiRequestException("User with ID " + updatedUser.getId() + " not found");
        }
    }


    public String generateToken(String email, Role role) {
        return jwtService.generateToken(email, role);
    }

    public void validateToken(String token){
        jwtService.validateToken(token);
    }

    public String getAccessToken(String email, String password) {
        Optional<UserCredential> userCredentialOptional = repository.findByEmail(email);

        if (userCredentialOptional.isPresent()) {
            UserCredential user = userCredentialOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return jwtService.generateToken(user.getEmail(), user.getRole());
            } else {
                throw new ApiRequestException("Invalid password");
            }
        } else {
            throw new ApiRequestException("User with email " + email + " not found");
        }
    }

}
