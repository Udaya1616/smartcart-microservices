package com.smartcart.userservice.dto;

public record AuthResponse(String token, UserResponse user) {
}
