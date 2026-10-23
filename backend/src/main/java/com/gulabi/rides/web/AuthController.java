package com.gulabi.rides.web;
import com.gulabi.rides.domain.*; import com.gulabi.rides.dto.Dto.*; import com.gulabi.rides.repo.*; import com.gulabi.rides.security.JwtService; import com.gulabi.rides.service.GoogleAuthService; import com.gulabi.rides.service.PasswordResetService; import java.util.Map; import java.util.UUID;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
  private final UserRepository users; private final DriverRepository drivers; private final PasswordEncoder encoder; private final JwtService jwt; private final GoogleAuthService google; private final PasswordResetService passwordReset;

  @PostMapping("/register") @Transactional
  public TokenResponse register(@Valid @RequestBody RegisterRequest q) {
    if (q.role() == Role.ADMIN) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admins are created by operators");
    if (users.existsByEmail(q.email().toLowerCase())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
    User u = new User(); u.setName(q.name()); u.setEmail(q.email().toLowerCase()); u.setPasswordHash(encoder.encode(q.password())); u.setRole(q.role());
    users.save(u);
    if (q.role() == Role.DRIVER) { Driver d = new Driver(); d.setUser(u); d.setVehicle(q.vehicle()); d.setPlate(q.plate()); drivers.save(d); }
    return new TokenResponse(jwt.generate(u), u.getRole(), u.getName());
  }

  @PostMapping("/login")
  public TokenResponse login(@Valid @RequestBody LoginRequest q) {
    User u = users.findByEmail(q.email().toLowerCase()).filter(x -> encoder.matches(q.password(), x.getPasswordHash()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password"));
    return new TokenResponse(jwt.generate(u), u.getRole(), u.getName());
  }

  /** Sign in with Google: the browser sends the ID token, we verify it, then issue our own JWT. New accounts default to RIDER. */
  @PostMapping("/google") @Transactional
  public TokenResponse google(@Valid @RequestBody GoogleLoginRequest q) {
    var p = google.verify(q.idToken());
    String email = p.getEmail().toLowerCase();
    boolean wantsDriver = q.role() == Role.DRIVER;
    if (wantsDriver && users.findByEmail(email).isEmpty() && (isBlank(q.vehicle()) || isBlank(q.plate())))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Drivers need to enter their vehicle and number plate");
    User u = users.findByEmail(email).orElseGet(() -> {
      User n = new User();
      Object name = p.get("name");
      n.setName(name == null ? email : name.toString()); n.setEmail(email);
      n.setPasswordHash(encoder.encode(UUID.randomUUID().toString())); // Google-only account: no usable password
      n.setRole(wantsDriver ? Role.DRIVER : Role.RIDER); users.save(n);
      if (wantsDriver) { Driver d = new Driver(); d.setUser(n); d.setVehicle(q.vehicle()); d.setPlate(q.plate()); drivers.save(d); }
      return n;
    });
    return new TokenResponse(jwt.generate(u), u.getRole(), u.getName());
  }

  private static boolean isBlank(String s) { return s == null || s.isBlank(); }

  @PostMapping("/forgot")
  public Map<String, String> forgot(@Valid @RequestBody ForgotRequest q) {
    passwordReset.request(q.email());
    return Map.of("message", "If that email has an account, we have sent a link to reset the password.");
  }

  @PostMapping("/reset")
  public Map<String, String> reset(@Valid @RequestBody ResetRequest q) {
    passwordReset.reset(q.token(), q.password());
    return Map.of("message", "Password updated. You can log in now.");
  }
}
