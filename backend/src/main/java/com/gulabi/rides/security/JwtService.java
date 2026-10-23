package com.gulabi.rides.security;
import com.gulabi.rides.domain.User;
import io.jsonwebtoken.*; import io.jsonwebtoken.io.Decoders; import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
import javax.crypto.SecretKey; import java.util.Date;

@Service
public class JwtService {
  private final SecretKey key; private final long ttlMs;
  public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.ttl-minutes}") long ttlMinutes) {
    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)); this.ttlMs = ttlMinutes * 60_000;
  }
  public String generate(User u) {
    return Jwts.builder().subject(u.getEmail()).claim("role", u.getRole().name())
        .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + ttlMs)).signWith(key).compact();
  }
  public Claims parse(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); }
}
