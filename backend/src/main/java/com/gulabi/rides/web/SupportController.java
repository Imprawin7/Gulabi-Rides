package com.gulabi.rides.web;
import com.gulabi.rides.domain.*; import com.gulabi.rides.repo.*; import jakarta.validation.Valid; import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; import java.security.Principal; import java.util.*;

@RestController @RequestMapping("/api/support") @RequiredArgsConstructor
public class SupportController {
  public record TicketRequest(@NotBlank @Size(max = 120) String subject, @NotBlank @Size(max = 2000) String body, Long rideId) {}
  private final SupportTicketRepository tickets; private final UserRepository users; private final RideRepository rides;

  @PostMapping public Map<String, Object> create(@Valid @RequestBody TicketRequest q, Principal p) {
    User u = users.findByEmail(p.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    SupportTicket t = new SupportTicket(); t.setUser(u); t.setSubject(q.subject()); t.setBody(q.body());
    if (q.rideId() != null) {
      Ride r = rides.findById(q.rideId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));
      if (!r.getRider().getEmail().equals(p.getName())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
      t.setRide(r);
    }
    tickets.save(t);
    return Map.of("id", t.getId(), "status", t.getStatus());
  }

  @GetMapping("/mine") public List<Map<String, Object>> mine(Principal p) {
    return tickets.findByUserEmail(p.getName()).stream().map(t -> Map.<String, Object>of("id", t.getId(), "subject", t.getSubject(), "status", t.getStatus())).toList();
  }
}
