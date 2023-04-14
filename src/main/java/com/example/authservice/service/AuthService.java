package com.example.authservice.service;

import com.example.authservice.Exception.ApiRequestException;
import com.example.authservice.dto.RestaurantDTO;
import com.example.authservice.entity.Role;
import com.example.authservice.entity.UserCredential;
import com.example.authservice.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
        String token = generateToken(savedUser.getName(), savedUser.getRole());
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
        return "user added to the system";
    }



    public String generateToken(String username, Role role) {
        return jwtService.generateToken(username, role);
    }

    public void validateToken(String token){
        jwtService.validateToken(token);
    }
}
