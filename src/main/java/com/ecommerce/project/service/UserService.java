package com.ecommerce.project.service;

import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.LoginResponse;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.response.UserinfoResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface UserService {
    MessageResponse registerUser(SignupRequest request);
    LoginResponse authenticateUser(LoginRequest request);
    UserinfoResponse getUserInfo(Authentication authentication);
    ResponseCookie cleanUpCookie();

    String updateUserRoles(Long userId, Set<String> roles);
}
