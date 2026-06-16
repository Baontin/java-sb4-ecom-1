package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.LoginResponse;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.response.UserinfoResponse;
import com.ecommerce.project.security.services.UserDetailsImpl;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Override
    @Transactional
    public MessageResponse registerUser(SignupRequest request) {
        // check unique fields
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new APIException("Username already taken.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new APIException("Username already taken.");
        }

        // create user
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );

        // set Role
        Role role = roleRepository.findByRoleName(AppRole.ROLE_USER)
                .orElseGet(() -> {
                    Role newUserRole = new Role(AppRole.ROLE_USER);
                    return roleRepository.save(newUserRole);
                });

        user.getRoles().add(role);
        userRepository.save(user);

        return new MessageResponse("User registered successfully!!");
    }

    @Override
    public LoginResponse authenticateUser(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(role -> role.getAuthority())
                .toList();
        // get jwt
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        // put info to response dto
        UserinfoResponse userinfoResponse = new UserinfoResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                roles
        );

        return new LoginResponse(userinfoResponse, jwtCookie);
    }

    @Override
    public UserinfoResponse getUserInfo(Authentication authentication) {
        if (authentication == null) {
            throw new APIException("Please login first.");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(role -> role.getAuthority())
                .toList();

        return new UserinfoResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                roles
        );
    }

    @Override
    public ResponseCookie cleanUpCookie() {
        ResponseCookie cookie = jwtUtils.cleanJwtCookie();
        return cookie;
    }

    @Override
    public String updateUserRoles(Long userId, Set<String> rolesName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Set<Role> roles = new HashSet<>();
        for (String roleName : rolesName) {
            Map<String, AppRole> roleMap = Map.of(
                    "user", AppRole.ROLE_USER,
                    "admin", AppRole.ROLE_ADMIN,
                    "seller", AppRole.ROLE_SELLER
            );

            AppRole appRole = roleMap.get(roleName);
            Role role = roleRepository.findByRoleName(appRole)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
            roles.add(role);
        }

        user.setRoles(roles);
        userRepository.save(user);
        return "User updated successfully!";
    }
}
