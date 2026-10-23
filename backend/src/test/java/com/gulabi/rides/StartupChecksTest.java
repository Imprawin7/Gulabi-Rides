package com.gulabi.rides;
import com.gulabi.rides.config.StartupChecks; import org.junit.jupiter.api.Test; import java.util.Base64;
import static org.assertj.core.api.Assertions.*;

class StartupChecksTest {
  static final String DEFAULT_JWT = Base64.getEncoder().encodeToString("change-me-to-a-long-random-secret-key-32bytes".getBytes());

  @Test void developmentModeAllowsDefaults() { assertThatCode(() -> new StartupChecks(false, DEFAULT_JWT, "change-me-now", "sk_test_replace_me").check()).doesNotThrowAnyException(); }
  @Test void productionRejectsPlaceholderSecrets() {
    assertThatThrownBy(() -> new StartupChecks(true, DEFAULT_JWT, "change-me-now", "sk_test_replace_me").check())
        .isInstanceOf(IllegalStateException.class).hasMessageContaining("JWT_SECRET").hasMessageContaining("ADMIN_PASSWORD").hasMessageContaining("STRIPE_SECRET");
  }
  @Test void productionAcceptsRealValues() { assertThatCode(() -> new StartupChecks(true, "c29tZS1yYW5kb20tc2VjcmV0LXZhbHVlLXRoYXQtaXMtbG9uZw==", "a-long-admin-passphrase", "sk_live_abc").check()).doesNotThrowAnyException(); }
}
