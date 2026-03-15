package com.example.springsec;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // STEP 1: Read the Authorization header
        // Client sends: Authorization: Bearer eyJhbGci...
        String authHeader = request.getHeader("Authorization");

        String token = null;
        String username = null;

        // STEP 2: Check header exists and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // remove "Bearer " prefix
            username = jwtUtils.getUsernameFromToken(token); // extract username
        }

        // STEP 3: If we got a username AND no one is already authenticated
        // SecurityContextHolder.getContext().getAuthentication() == null
        // means Spring doesn't know who this user is yet
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // STEP 4: Load full user details from DB
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // STEP 5: Validate the token
            if (jwtUtils.validateToken(token)) {

                // STEP 6: Create authentication object
                // This is what Spring Security uses internally to represent "logged in user"
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,       // the principal (who)
                                null,              // credentials (null because JWT, no password needed)
                                userDetails.getAuthorities()  // roles
                        );

                // attach request details (IP, session etc.)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // STEP 7: Put the user in SecurityContext
                // From this point, Spring Security knows the user is authenticated
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // STEP 8: Pass request to next filter regardless
        // If token was invalid, SecurityContext stays empty → Spring returns 401
        filterChain.doFilter(request, response);
    }
}

