package com.example.e_commerce_api.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    
    // Get Secret Key from application.properties
    @Value("${app.jwt-secret}")
    private String jwtSecret;

    // Get Token expiration time from application.properties (e.g., 604800000ms = 7 days)
    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    // 1. Generate the token
    public String generateToken(Authentication authentication) {
        // 1. Get the username (email)
        String username = authentication.getName();

        // 2. Extract the Authority/Role as a string (e.g., "ADMIN" or "USER")
        // Note: We use findFirst().get() assuming a user has exactly one role/authority.
        String role = authentication.getAuthorities().stream()
            .findFirst()
            .map(authority -> authority.getAuthority())
            .orElseThrow(() -> new RuntimeException("User has no role/authority.")); 

        // 3. Build the JWT
        return Jwts.builder()
            .setSubject(username)
            // CRITICAL: The claim name MUST be "role" to match your JwtAuthenticationFilter
            .claim("role", role) 
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours validity
            .signWith(key(), SignatureAlgorithm.HS512)
            .compact();
    }

    // Key generation from the secret string
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // 2. Extract username from token
    public String getUsernameFromJWT(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 3. Validate the token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key()) // Uses the secure 512-bit key
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            // Log "Invalid JWT signature"
        } catch (ExpiredJwtException e) {
            // Log "Expired JWT token"
        } catch (UnsupportedJwtException e) {
            // Log "Unsupported JWT token"
        } catch (IllegalArgumentException e) {
            // Log "JWT claims string is empty"
        }
        return false;
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}