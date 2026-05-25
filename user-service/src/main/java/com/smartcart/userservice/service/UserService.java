package com.smartcart.userservice.service;

import com.smartcart.userservice.dto.AuthResponse;
import com.smartcart.userservice.dto.LoginRequest;
import com.smartcart.userservice.dto.RegisterRequest;
import com.smartcart.userservice.dto.UserResponse;
import com.smartcart.userservice.entity.User;
import com.smartcart.userservice.exception.BadRequestException;
import com.smartcart.userservice.exception.ResourceNotFoundException;
import com.smartcart.userservice.repository.UserRepository;
import com.smartcart.userservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("USER");
        User savedUser = userRepository.save(user);
        log.info("Registered user {} with role {}", savedUser.getUsername(), savedUser.getRole());
        return toResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadRequestException("Invalid username or password");
        }
        return new AuthResponse(jwtService.generateToken(user), toResponse(user));
    }

    public UserResponse getUser(Long id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
