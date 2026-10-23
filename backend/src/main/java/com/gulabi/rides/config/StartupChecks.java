package com.gulabi.rides.config;
import jakarta.annotation.PostConstruct; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets; import java.util.*;

/** With APP_PRODUCTION=true the API refuses to start on placeholder secrets. */
@Component
public class StartupChecks {
  private static final String DEFAULT_JWT = Base64.getEncoder().encodeToString("change-me-to-a-long-random-secret-key-32bytes".getBytes(StandardCharsets.UTF_8));
  private final boolean production; private final String jwtSecret, adminPassword, stripeSecret;

  public StartupChecks(@Value("${app.production}") boolean production, @Value("${app.jwt.secret}") String jwtSecret,
                       @Value("${app.admin-password}") String adminPassword, @Value("${stripe.secret}") String stripeSecret) {
    this.production = production; this.jwtSecret = jwtSecret; this.adminPassword = adminPassword; this.stripeSecret = stripeSecret;
  }

  @PostConstruct public void check() {
    if (!production) return;
    List<String> problems = new ArrayList<>();
    if (DEFAULT_JWT.equals(jwtSecret)) problems.add("JWT_SECRET is still the built-in default");
    if ("change-me-now".equals(adminPassword) || adminPassword.length() < 12) problems.add("ADMIN_PASSWORD must be changed and at least 12 characters");
    if (stripeSecret.contains("replace_me")) problems.add("STRIPE_SECRET is not set");
    if (!problems.isEmpty()) throw new IllegalStateException("Refusing to start in production: " + String.join("; ", problems));
  }
}
