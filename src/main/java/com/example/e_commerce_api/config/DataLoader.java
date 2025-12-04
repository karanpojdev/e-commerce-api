package com.example.e_commerce_api.config;

import com.example.e_commerce_api.model.User;
import com.example.e_commerce_api.model.Role;
import com.example.e_commerce_api.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    // Spring will automatically inject these components
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            // 1. Check if an admin user already exists to prevent duplicates
            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                
                // 2. Create the Admin User object
                User adminUser = new User();
                adminUser.setName("System Admin");
                adminUser.setEmail("admin@example.com");
                
                // 3. Encrypt the password using your PasswordEncoder bean
                String encodedPassword = passwordEncoder.encode("SecureAdminPassword123!");
                adminUser.setPassword(encodedPassword);
                
                // 4. Assign the ADMIN role
                adminUser.setRole(Role.ADMIN);

                // 5. Save to the database
                userRepository.save(adminUser);
                
                System.out.println(">>> Initial Admin User Created: admin@example.com");
            }
        };
    }
}