package com.example.e_commerce_api.security;

import com.example.e_commerce_api.service.CustomUserDetailsService;
import com.example.e_commerce_api.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired private JwtService jwtService;
    @Autowired private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = getJwtFromRequest(request);

            // 1. Check if token exists AND is valid (using JwtService)
            if (StringUtils.hasText(token) && jwtService.validateToken(token)) { 
                
                // Get the username (subject) from the token payload
                String username = jwtService.getUsernameFromToken(token); 
                
                // 2. Load the UserDetails object using the username
                // This step is crucial because it loads the user's details AND the CORRECT authority from the database.
                UserDetails userDetails = userDetailsService.loadUserByUsername(username); 

                if (userDetails != null) {
                    // 3. Create Authentication token using UserDetails (which contains the correct authorities)
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, // credentials null since token is validated
                            userDetails.getAuthorities() // Authorities loaded from UserDetails
                    );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 4. Set the authentication in the security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            // Log the exception, but continue the filter chain
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}