package recipeSharing.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import recipeSharing.service.AuthUserDetailsService;
import recipeSharing.service.JWTService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthUserDetailsService authUserDetailsService;
    private final JWTService jwtService;

    public SecurityConfig(AuthUserDetailsService authUserDetailsService, JWTService jwtService) {
        this.authUserDetailsService = authUserDetailsService;
        this.jwtService = jwtService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable()) // Consider enabling CSRF protection in production
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/users/register", "/users/login", "/users/logout", "/recipes/create").permitAll()
                        .requestMatchers(HttpMethod.GET, "/recipes/user/{username}", "/recipes/{id}", "/recipes/all", "/groups/all", "/prices").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/recipes/update/{id}","/recipes/favorite/${id}").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/recipes/delete/{id}").permitAll()
                        .anyRequest().authenticated()) // All other endpoints require authentication
                .userDetailsService(authUserDetailsService) // Custom UserDetailsService
                .logout(logout -> logout
                        .logoutUrl("/users/logout") // Define the logout URL
                        .logoutSuccessUrl("/users/login") // Redirect URL after successful logout
                        .invalidateHttpSession(true) // Invalidate the session
                        .deleteCookies("JSESSIONID") // Optionally delete cookies
                        .addLogoutHandler((request, response, authentication) -> {
                            // Blacklist the JWT token if applicable
                            String token = extractTokenFromRequest(request);
                            if (token != null) {
                                jwtService.blacklistToken(token);
                            }
                        })).build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(authUserDetailsService);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Helper method to extract JWT token from request
    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
