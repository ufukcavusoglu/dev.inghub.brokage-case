package dev.inghub.brokerage.dto;

public class AuthDtos {
    public record LoginRequest(String username, String password) {}
    public record TokenResponse(String token, String customerId) {}
}
