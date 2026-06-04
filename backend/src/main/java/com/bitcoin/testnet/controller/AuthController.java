package com.bitcoin.testnet.controller;

import com.bitcoin.testnet.document.User;
import com.bitcoin.testnet.dto.request.AuthRequest;
import com.bitcoin.testnet.dto.response.AuthResponse;
import com.bitcoin.testnet.repository.UserRepository;
import com.bitcoin.testnet.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();
        
        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        return ResponseEntity.ok(AuthResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();
        
        String jwtToken = jwtService.generateToken(user);
        return ResponseEntity.ok(AuthResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .build());
    }
}
