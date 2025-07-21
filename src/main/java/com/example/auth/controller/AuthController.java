package com.example.auth.controller;

import com.example.auth.dto.JwtResponse;
//import com.example.auth.dto.SendOtpRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.VerifyOtpRequest;
import com.example.auth.dto.LoginRequest;
import com.example.auth.jwt.JwtService;
import com.example.auth.model.Role;
import com.example.auth.model.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private final Map<String, String> otpStorage = new HashMap<>();

    public AuthController(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = new User();
        user.setName(request.getName());
        user.setEmailId(request.getEmailId());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDefaultRole(defaultRole);
        user.setActive(true);
        user.setVerified(false);

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User registered"));
    }

//    @PostMapping("/send-otp")
//    @Operation(summary = "Send OTP to phone")
//    public ResponseEntity<?> sendOtp(@RequestBody SendOtpRequest request) {
//        String code = String.valueOf(new Random().nextInt(899999) + 100000);
//        otpStorage.put(request.getPhoneNumber(), code);
//        System.out.println("OTP for " + request.getPhoneNumber() + ": " + code);
//        return ResponseEntity.ok(Map.of("message", "OTP sent", "otp", code)); // dev only
//    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP and generate JWT")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest otpRequest) {
        String savedCode = otpStorage.get(otpRequest.getPhoneNumber());
        if (savedCode != null && savedCode.equals(otpRequest.getCode())) {
            Optional<User> userOpt = userRepository.findByPhoneNumber(otpRequest.getPhoneNumber());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setVerified(true);
                userRepository.save(user);

                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
                String token = jwtService.generateAccessToken(userDetails);

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserId());
                userInfo.put("name", user.getName());
                userInfo.put("email", user.getEmailId());
                userInfo.put("username", user.getUsername());
                userInfo.put("phone", user.getPhoneNumber());
                userInfo.put("roleId", user.getDefaultRole().getId()); // or .getName()
                userInfo.put("active", user.getActive());

                return ResponseEntity.ok(Map.of(
                        "jwt", token,
                        "user", userInfo
                ));
            }
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid OTP or phone number"));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with username and password")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            //String token = jwtService.generateAccessToken(userDetails);
            // Generate OTP and store
            String code = String.valueOf(new Random().nextInt(899999) + 100000);
            otpStorage.put(user.getPhoneNumber(), code);

            System.out.println("OTP for " + user.getPhoneNumber() + ": " + code);

            //return ResponseEntity.ok(Map.of("jwt", token));

            return ResponseEntity.ok(Map.of(
                    "message", "OTP sent to your registered phone number",
                    "phone", user.getPhoneNumber(),
                    "otp", code // Only for dev/testing
            ));

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/protected")
    public ResponseEntity<?> protectedApi() {
        return ResponseEntity.ok("This is a protected endpoint");
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<?> adminEndpoint() {
        return ResponseEntity.ok("Hello, Admin!");
    }
}
