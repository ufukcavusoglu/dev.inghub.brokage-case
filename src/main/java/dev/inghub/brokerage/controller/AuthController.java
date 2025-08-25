package dev.inghub.brokerage.controller;

import dev.inghub.brokerage.dto.AuthDtos;
import dev.inghub.brokerage.entity.Customer;
import dev.inghub.brokerage.security.JwtService;
import dev.inghub.brokerage.repo.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomerRepository customerRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.TokenResponse> login(@RequestBody AuthDtos.LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        var customer = customerRepository.findByUsername(req.username()).orElseThrow();
        String token = jwtService.generate(customer);
        return ResponseEntity.ok(new AuthDtos.TokenResponse(token, customer.getCustomerId()));
    }

    // Helper endpoint to create a demo customer quickly (not for production)
    @PostMapping("/signup-demo")
    public ResponseEntity<?> createDemo(@RequestBody AuthDtos.LoginRequest req) {
        if (customerRepository.findByUsername(req.username()).isPresent()) return ResponseEntity.badRequest().body("exists");
        Customer c = Customer.builder()
                .username(req.username())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role("CUSTOMER")
                .customerId("CUST-" + req.username().toUpperCase())
                .build();
        customerRepository.save(c);
        return ResponseEntity.ok().build();
    }
}
