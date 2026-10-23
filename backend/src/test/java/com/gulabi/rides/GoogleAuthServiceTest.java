package com.gulabi.rides;
import com.gulabi.rides.service.GoogleAuthService; import org.junit.jupiter.api.Test; import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.*;

class GoogleAuthServiceTest {
  @Test void unavailableWhenNoClientIdConfigured() {
    assertThatThrownBy(() -> new GoogleAuthService("").verify("anything")).isInstanceOf(ResponseStatusException.class).hasMessageContaining("503");
  }
  @Test void garbageTokensAreRejected() {
    assertThatThrownBy(() -> new GoogleAuthService("client-id.apps.googleusercontent.com").verify("not-a-jwt")).isInstanceOf(ResponseStatusException.class).hasMessageContaining("401");
  }
}
