package com.example.springsec;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

    @Component
    public class JwtUtils {


        @Value("${jwt.secret}")          String secret;
        @Value("${jwt.expiration-ms}")   long   expirationMs;

        // The key used to sign and verify the token
        private Key getSigningKey() {
            return Keys.hmacShaKeyFor(secret.getBytes());
        }

        // ✅ Called after login — creates the token
        public String generateToken(UserDetailsImpl userDetails) {
            return Jwts.builder()
                    .setSubject(userDetails.getUsername())    // who the token belongs to
                    .claim("role", userDetails.getAuthorities() // store role inside token
                            .iterator().next().getAuthority())
                    .setIssuedAt(new Date())                  // when it was created
                    .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                    .signWith(getSigningKey())                // sign it so nobody can tamper
                    .compact();                               // build the final string
        }

        // ✅ Called in JwtFilter — extracts username from token
        public String getUsernameFromToken(String token) {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();  // "subject" = username we set above
        }

        // ✅ Called in JwtFilter — checks token is valid and not expired
        public boolean validateToken(String token) {
            try {
                Jwts.parserBuilder()
                        .setSigningKey(getSigningKey())
                        .build()
                        .parseClaimsJws(token);  // throws exception if invalid
                return true;
            } catch (ExpiredJwtException e) {
                System.out.println("Token expired");
            } catch (MalformedJwtException e) {
                System.out.println("Invalid token");
            } catch (SignatureException e) {
                System.out.println("Invalid signature — token was tampered!");
            }
            return false;
        }
    }
