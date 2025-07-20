package com.example.auth.controller;

import com.example.auth.dto.JwtResponse;
import com.example.auth.dto.SendOtpRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.VerifyOtpRequest;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final Map<String, String> otpStorage = new HashMap<>();
    private final PasswordEncoder passwordEncoder;


    public AuthController(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmailId(request.getEmailId());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // 🔒 Encrypt if needed
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDefaultRoleId(request.getDefaultRoleId());
        user.setActive(true);
        user.setVerified(false);


        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User registered"));
    }

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP to phone")
    public ResponseEntity<?> sendOtp(@RequestBody SendOtpRequest request) {
        String code = String.valueOf(new Random().nextInt(899999) + 100000);
        otpStorage.put(request.getPhoneNumber(), code);
        System.out.println("OTP for " + request.getPhoneNumber() + ": " + code);
        return ResponseEntity.ok(Map.of("message", "OTP sent", "otp", code)); // only for dev
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP and generate JWT")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest  otp) {
        String savedCode = otpStorage.get(otp.getPhoneNumber());
        if (savedCode != null && savedCode.equals(otp.getCode())) {
            Optional<User> userOpt = userRepository.findByPhoneNumber(otp.getPhoneNumber());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setVerified(true);
                userRepository.save(user);

                String token = jwtUtil.generateToken(user.getUsername());

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserId());
                userInfo.put("name", user.getName());
                userInfo.put("email", user.getEmailId());
                userInfo.put("username", user.getUsername());
                userInfo.put("phone", user.getPhoneNumber());
                userInfo.put("roleId", user.getDefaultRoleId());
                userInfo.put("active", user.getActive());


                return ResponseEntity.ok(Map.of(
                        "jwt", token,
                        "user", userInfo
                ));
            }
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid OTP or phone number"));
    }
}
