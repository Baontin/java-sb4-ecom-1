package com.ecommerce.project.controller;

import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.response.UserinfoResponse;
import com.ecommerce.project.security.services.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager , JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/auth/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = null;
        // Authenticate user
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
            );
        } catch (AuthenticationException e) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("status", false);
            return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
        }

        // Put authenticated user into SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Take user details
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // get name, roles,token
        List<String> roles = userDetails.getAuthorities().stream()
                .map(role -> role.getAuthority())
                .toList();
        String token = jwtUtils.generateJwtFromUsername(userDetails);
        // put them into UserinfoResponse
        UserinfoResponse response = new UserinfoResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                roles,
                token
        );
        // give back to client
        return ResponseEntity.ok(response);
    }
}
