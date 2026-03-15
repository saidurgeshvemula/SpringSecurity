package com.example.springsec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity          // activates Spring Security
@EnableMethodSecurity       // enables @PreAuthorize on controllers
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // BEAN 1: PasswordEncoder
    // BCrypt hashes passwords before saving, and verifies them on login
    // NEVER store plain text passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // BEAN 2: DaoAuthenticationProvider
    // This connects UserDetailsService + PasswordEncoder together
    // Spring uses this during login to: fetch user → compare password
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // BEAN 3: AuthenticationManager
    // This is what you call in your AuthController to trigger login
    // Spring internally uses DaoAuthenticationProvider to do the actual work
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // BEAN 4: SecurityFilterChain — THE MAIN CONFIG
    // This defines the rules for every incoming request
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF — not needed for stateless JWT APIs
                // CSRF is for browser-based sessions, not tokens
                .csrf(csrf -> csrf.disable())

                // Define which endpoints need what level of access
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()   // login & register = public
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")  // admin only
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()                  // everything else needs login
                )

                // Tell Spring: don't create sessions
                // Each request must carry its own JWT — fully stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Register our DaoAuthenticationProvider
                .authenticationProvider(authenticationProvider())

                // ⭐ THE KEY LINE:
                // Add JwtAuthFilter BEFORE the default login filter
                // So our filter runs first, reads the JWT, sets the user in context
                // By the time UsernamePasswordAuthenticationFilter runs, user is already set
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
