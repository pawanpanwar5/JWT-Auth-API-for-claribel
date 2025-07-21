package com.example.auth.config;

import com.example.auth.model.Role;
import com.example.auth.model.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedRolesAndAdmin(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // Seed roles
            if (roleRepository.count() == 0) {
                Role adminRole = new Role();
                adminRole.setName("ROLE_ADMIN");
                adminRole.setPrivilege("All Access");
                adminRole.setActive(true);

                Role userRole = new Role();
                userRole.setName("ROLE_USER");
                userRole.setPrivilege("Basic Access");
                userRole.setActive(true);

                roleRepository.save(adminRole);
                roleRepository.save(userRole);
            }

            // Seed admin user
            if (userRepository.findByUsername("admin").isEmpty()) {
                Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                        .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

                User admin = new User();
                admin.setName("Super Admin");
                admin.setUsername("admin");
                admin.setEmailId("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin123"));  // 🔐 encoded password
                admin.setPhoneNumber("9999999999");
                admin.setDefaultRole(adminRole); // ✅ Set full Role object
                admin.setActive(true);
                admin.setVerified(true);

                userRepository.save(admin);

                System.out.println("✅ Default admin user created: admin / admin123");
            }
        };
    }
}
