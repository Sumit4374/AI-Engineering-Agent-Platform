package sumit.ai.ai_engineering.user.JWT;
import sumit.ai.ai_engineering.user.Configuration.CustomUserDetails.CustomUserDetails;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration-ms}")
    private Long expiration;

    private Key getSigningKey(){
        return Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(UserDetails userDetails){

        CustomUserDetails customUserDetails = (CustomUserDetails)userDetails;

        return Jwts.builder()
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(new Date(
            System.currentTimeMillis() + expiration 
        ))
        .signWith(getSigningKey())
        .claim("role", customUserDetails.getRole())
        .claim("userId", customUserDetails.getId())
        .compact();
    }

    public Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
    }

    public String extractUserName(String token){
        return extractAllClaims(token).getSubject();
    }

    public Date extractExpiration(String token){
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token){
        Long currentTime =  System.currentTimeMillis();
        Long expirationTime = extractExpiration(token).getTime();
        return currentTime > expirationTime;
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        String userName = extractUserName(token);
        return userName.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public <T> T extractClaims(String token, Function<Claims,T> resolver){
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    public Long extractUserId(String token){
        return (Long)extractClaims(token, 
            claims -> claims.get("userId", Long.class)
        );
    }

    public String extractRole(String token){
        return extractClaims(token, 
            claims -> claims.get("role",String.class)
        );
    }
}
