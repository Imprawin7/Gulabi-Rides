package com.gulabi.rides.web;
import org.springframework.messaging.handler.annotation.*; import org.springframework.stereotype.Controller; import java.security.Principal;

@Controller
public class RealtimeController {
  public record Location(double lat, double lng) {}
  public record Sos(Long rideId, String message, double lat, double lng) {}

  /** Driver app publishes to /app/ride/{id}/location; rider subscribes to /topic/ride/{id}/location. */
  @MessageMapping("/ride/{id}/location") @SendTo("/topic/ride/{id}/location")
  public Location location(@DestinationVariable Long id, Location l) { return l; }

  /** Rider app publishes to /app/sos; the admin console subscribes to /topic/admin/sos. */
  @MessageMapping("/sos") @SendTo("/topic/admin/sos")
  public Sos sos(Sos s, Principal p) { return new Sos(s.rideId(), p.getName() + ": " + s.message(), s.lat(), s.lng()); }
}
