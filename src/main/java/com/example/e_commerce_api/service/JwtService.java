package com.example.e_commerce_api.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    
    // Get Secret Key from application.properties
    @Value("${app.jwt-secret}")
    private String jwtSecret;

    // Get Token expiration time from application.properties (e.g., 604800000ms = 7 days)
    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    // 1. Generate the token
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                // We use a strong key for signing
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
        return token;
    }

    // Key generation from the secret string
    private Key key() {
        logger.info("JWT Secret Key being used (first 10 chars): {}", jwtSecret.substring(0, Math.min(jwtSecret.length(), 10)));
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
            Jwts.parserBuilder().setSigningKey(key()).build().parse(token);
            return true;
        } catch (Exception e) {
            // Log error here (e.g., token expired, invalid signature, etc.)
            return false;
        }
    }
}