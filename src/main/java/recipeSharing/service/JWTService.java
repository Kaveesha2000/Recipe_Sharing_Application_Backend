package recipeSharing.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import recipeSharing.entity.AuthUser;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
public class JWTService {

    private Key secretKey;

    // Load token expiration time from application properties (configurable)
    @Value("${jwt.expiration}")
    private long tokenExpirationMs;

    // In-memory blacklisting (you may want to persist this in a DB or cache for scalability)
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    // Generate the secret key using the HS256 algorithm during bean initialization
    @PostConstruct
    public void init() {
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Or load from environment variables or keystore
    }

    // Generate token for user
    public String generateToken(AuthUser user) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, user.getUsername());
    }

    // Create token with claims, subject (username), issued date, expiration, and signature
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpirationMs)) // Configurable expiration
                .signWith(secretKey) // Use securely managed key
                .compact();
    }

    // Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract a specific claim from the token using a claim resolver function
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from the token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey) // Verify using the same key used for signing
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Check if the token has expired by comparing the expiration date to the current date
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Validate the token: check if username matches, token isn't expired, and it's not blacklisted
    public Boolean isTokenValid(String token, AuthUser user) {
        final String username = extractUsername(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token) && !blacklistedTokens.contains(token));
    }

    // Blacklist a token (add to in-memory set or persist in database for scalability)
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    // Remove token from blacklist (if you want to allow reactivation)
    public void removeTokenFromBlacklist(String token) {
        blacklistedTokens.remove(token);
    }
}
