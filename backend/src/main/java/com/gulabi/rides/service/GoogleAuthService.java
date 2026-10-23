package com.gulabi.rides.service;
import com.google.api.client.googleapis.auth.oauth2.*; import com.google.api.client.http.javanet.NetHttpTransport; import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value; import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException; import java.io.IOException; import java.security.GeneralSecurityException; import java.util.List;

@Service
public class GoogleAuthService {
  private final GoogleIdTokenVerifier verifier; // null when GOOGLE_CLIENT_ID is not configured

  public GoogleAuthService(@Value("${app.google-client-id}") String clientId) {
    this.verifier = clientId == null || clientId.isBlank() ? null
        : new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance()).setAudience(List.of(clientId)).build();
  }

  /** Verifies signature, audience and expiry, and requires a verified email. */
  public GoogleIdToken.Payload verify(String idToken) {
    if (verifier == null) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Google sign-in is not configured");
    try {
      GoogleIdToken t = verifier.verify(idToken);
      if (t == null || !Boolean.TRUE.equals(t.getPayload().getEmailVerified())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token");
      return t.getPayload();
    } catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token");
    }
  }
}
