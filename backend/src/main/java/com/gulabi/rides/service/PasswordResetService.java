package com.gulabi.rides.service;
import com.gulabi.rides.domain.PasswordResetToken; import com.gulabi.rides.repo.*; import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; import org.springframework.http.HttpStatus; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets; import java.security.*; import java.time.*; import java.util.*;

@Service @RequiredArgsConstructor
public class PasswordResetService {
  private static final SecureRandom RNG = new SecureRandom();
  private final UserRepository users; private final PasswordResetTokenRepository tokens; private final PasswordEncoder encoder; private final MailService mail;
  @Value("${app.public-url}") private String publicUrl;

  /** Always returns normally, whether or not the email exists, so the endpoint cannot be used to discover accounts. */
  @Transactional
  public void request(String email) {
    users.findByEmail(email.trim().toLowerCase()).ifPresent(u -> {
      tokens.deleteByUserId(u.getId());
      byte[] b = new byte[32]; RNG.nextBytes(b);
      String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
      PasswordResetToken t = new PasswordResetToken();
      t.setUser(u); t.setTokenHash(sha256(raw)); t.setExpiresAt(Instant.now().plus(Duration.ofMinutes(30)));
      tokens.save(t); // only the hash is stored; the raw token exists only in the emailed link
      mail.sendPasswordReset(u.getEmail(), u.getName(), publicUrl + "/reset?token=" + raw);
    });
  }

  @Transactional
  public void reset(String rawToken, String newPassword) {
    PasswordResetToken t = tokens.findByTokenHash(sha256(rawToken))
        .filter(x -> !x.isUsed() && x.getExpiresAt().isAfter(Instant.now()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "This reset link is invalid or has expired"));
    var u = t.getUser();
    u.setPasswordHash(encoder.encode(newPassword)); users.save(u);
    t.setUsed(true); tokens.save(t);
  }

  static String sha256(String s) {
    try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8))); }
    catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
  }
}
