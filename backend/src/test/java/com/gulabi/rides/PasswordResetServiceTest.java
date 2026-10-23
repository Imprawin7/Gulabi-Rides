package com.gulabi.rides;
import com.gulabi.rides.domain.*; import com.gulabi.rides.repo.*; import com.gulabi.rides.service.*;
import org.junit.jupiter.api.*; import org.mockito.ArgumentCaptor; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils; import org.springframework.web.server.ResponseStatusException;
import java.time.Instant; import java.util.Optional;
import static org.assertj.core.api.Assertions.*; import static org.mockito.ArgumentMatchers.*; import static org.mockito.Mockito.*;

class PasswordResetServiceTest {
  UserRepository users = mock(UserRepository.class); PasswordResetTokenRepository tokens = mock(PasswordResetTokenRepository.class);
  PasswordEncoder enc = mock(PasswordEncoder.class); MailService mail = mock(MailService.class);
  PasswordResetService svc = new PasswordResetService(users, tokens, enc, mail);
  User user = new User();

  @BeforeEach void setUp() {
    ReflectionTestUtils.setField(svc, "publicUrl", "https://app.test");
    user.setId(1L); user.setEmail("a@x.com"); user.setName("Asha"); user.setPasswordHash("OLD");
    when(users.findByEmail("a@x.com")).thenReturn(Optional.of(user));
    when(enc.encode(any())).thenReturn("HASHED");
  }

  String requestLink(ArgumentCaptor<PasswordResetToken> saved) {
    svc.request("A@x.com ");
    ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
    verify(mail).sendPasswordReset(eq("a@x.com"), eq("Asha"), link.capture());
    verify(tokens).save(saved.capture());
    return link.getValue();
  }

  @Test void unknownEmailSendsNothingAndDoesNotFail() {
    when(users.findByEmail("nobody@x.com")).thenReturn(Optional.empty());
    svc.request("nobody@x.com");
    verifyNoInteractions(mail);
  }

  @Test void linkCarriesTheRawTokenButOnlyAHashIsStored() {
    var saved = ArgumentCaptor.forClass(PasswordResetToken.class);
    String link = requestLink(saved);
    assertThat(link).startsWith("https://app.test/reset?token=");
    String raw = link.substring(link.indexOf("token=") + 6);
    assertThat(saved.getValue().getTokenHash()).hasSize(64).isNotEqualTo(raw);
    assertThat(saved.getValue().getExpiresAt()).isAfter(Instant.now());
  }

  @Test void aValidTokenChangesThePasswordExactlyOnce() {
    var saved = ArgumentCaptor.forClass(PasswordResetToken.class);
    String raw = requestLink(saved).split("token=")[1];
    PasswordResetToken t = saved.getValue();
    when(tokens.findByTokenHash(t.getTokenHash())).thenReturn(Optional.of(t));
    svc.reset(raw, "newpassword1");
    assertThat(user.getPasswordHash()).isEqualTo("HASHED"); assertThat(t.isUsed()).isTrue();
    assertThatThrownBy(() -> svc.reset(raw, "another-pass1")).isInstanceOf(ResponseStatusException.class).hasMessageContaining("400");
  }

  @Test void expiredAndUnknownTokensAreRejected() {
    var saved = ArgumentCaptor.forClass(PasswordResetToken.class);
    String raw = requestLink(saved).split("token=")[1];
    PasswordResetToken t = saved.getValue(); t.setExpiresAt(Instant.now().minusSeconds(5));
    when(tokens.findByTokenHash(t.getTokenHash())).thenReturn(Optional.of(t));
    assertThatThrownBy(() -> svc.reset(raw, "newpassword1")).isInstanceOf(ResponseStatusException.class);
    assertThatThrownBy(() -> svc.reset("garbage", "newpassword1")).isInstanceOf(ResponseStatusException.class);
    assertThat(user.getPasswordHash()).isEqualTo("OLD");
  }
}
