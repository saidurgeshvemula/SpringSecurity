package com.example.springsec;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")

public class UserController {

        // Any logged in user can access this
        @GetMapping("/user/profile")
        public ResponseEntity<String> profile() {

            // Read who is logged in from SecurityContext
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            return ResponseEntity.ok("Hello " + username + "! This is your profile.");
        }

        // Only ADMIN role can access this
        @GetMapping("/admin/dashboard")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<String> dashboard() {
            return ResponseEntity.ok("Welcome Admin! This is the dashboard.");
        }
    }

