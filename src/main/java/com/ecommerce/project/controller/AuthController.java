package com.ecommerce.project.controller;

import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.request.UpdateRolesRequest;
import com.ecommerce.project.security.response.LoginResponse;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.response.UserinfoResponse;
import com.ecommerce.project.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager , JwtUtils jwtUtils,
                          UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signin")
    public ResponseEntity<UserinfoResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        LoginResponse response = userService.authenticateUser(loginRequest);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, response.getCookie().toString())
                .body(response.getUserinfo());
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignupRequest request) {
        MessageResponse response = userService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/admin/update-roles/{userId}")
    public ResponseEntity<?> updateRoles(@PathVariable Long userId,
                                         @RequestBody UpdateRolesRequest request) {
        String rolesUpdated = userService.updateUserRoles(userId, request.getRoles());
        return new ResponseEntity<>(rolesUpdated, HttpStatus.OK);
    }

    @GetMapping("/username")
    public String getUsername(Authentication authentication) {
        if (authentication != null) {
            return authentication.getName();
        } else {
            return "Please login first";
        }
    }

    @GetMapping("/user")
     public ResponseEntity<UserinfoResponse> getUserDetails(Authentication authentication) {
        UserinfoResponse response = userService.getUserInfo(authentication);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signoutUser() {
        ResponseCookie cookie = userService.cleanUpCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("Signed out successfully!!"));
    }
}
