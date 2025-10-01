package com.claropr.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.function.Function;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class JwtUtil {
  private final String SECRET_KEY = "mySecret12ClaroPR34";
  private Date revokedAt;

  public String extractUsername(String token) {
    return this.extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    return this.extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = this.extractAllClaims(token);

    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
  }

  private Boolean isTokenExpired(String token) {
    return this.extractExpiration(token).before(new Date());
  }

  public String generateToken(String username) {
    return this.createToken(username);
  }

  private String createToken(String username) {
    revokedAt = new Date(System.currentTimeMillis() + 1000 * 60 * 10);

    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(revokedAt)
        .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
        .compact();
  }

  public Boolean validateToken(String token, String username) {
    final String extractedUsername = this.extractUsername(token);

    return (extractedUsername.equals(username) && !this.isTokenExpired(token));
  }
}

