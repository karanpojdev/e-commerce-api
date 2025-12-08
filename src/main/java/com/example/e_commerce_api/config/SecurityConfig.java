package com.example.e_commerce_api.config;

import com.example.e_commerce_api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Inject the filter as a bean so it can be managed by Spring (and Autowired)
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        /*http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/webhooks/**") // Disable CSRF for webhooks
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Use JWT (stateless)
            .authorizeHttpRequests(authorize -> authorize
                
                // Public Routes - Allow access without token
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/webhooks/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll() // Anyone can view products

                // Admin-Only Routes - Requires ADMIN role
                .requestMatchers(HttpMethod.POST, "/api/products").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAuthority("ADMIN")

                // Authenticated User Routes (USER or ADMIN)
                .requestMatchers("/api/cart/**").authenticated() // NEW: Protect cart endpoints
                .requestMatchers("/api/cart/**", "/api/orders/**").authenticated()
                
                // All other requests require authentication by default
                .anyRequest().authenticated() // All other requests require a logged-in user
            );
        
        // Add the JWT filter BEFORE the standard Spring Security filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();*/
        return http
            .csrf(csrf -> csrf.disable()) // Disable CSRF
            .cors(cors -> cors.disable()) // Disable CORS (if active)
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll()) // Allow ALL traffic
            .build();
    }
}