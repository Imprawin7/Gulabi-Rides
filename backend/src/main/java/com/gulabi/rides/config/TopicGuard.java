package com.gulabi.rides.config;
import com.gulabi.rides.repo.RideRepository; import lombok.RequiredArgsConstructor; import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component; import java.util.regex.*;

/** Decides who may subscribe to / publish on which STOMP destination. */
@Component @RequiredArgsConstructor
public class TopicGuard {
  private static final Pattern RIDE_TOPIC = Pattern.compile("^/topic/ride/(\\d+)(/location)?$");
  private static final Pattern LOCATION_SEND = Pattern.compile("^/app/ride/(\\d+)/location$");
  private final RideRepository rides;

  public boolean canSubscribe(Authentication a, String dest) {
    if (a == null || dest == null) return false;
    if (dest.equals("/user/queue/requests")) return has(a, "DRIVER");
    if (dest.startsWith("/topic/admin/")) return has(a, "ADMIN");
    Matcher m = RIDE_TOPIC.matcher(dest);
    if (!m.matches()) return false;
    if (has(a, "ADMIN")) return true;
    return rides.findById(Long.valueOf(m.group(1))).map(r -> r.getRider().getEmail().equals(a.getName())
        || (r.getDriver() != null && r.getDriver().getUser().getEmail().equals(a.getName()))).orElse(false);
  }

  public boolean canSend(Authentication a, String dest) {
    if (a == null || dest == null) return false;
    if (dest.equals("/app/sos")) return has(a, "RIDER");
    Matcher m = LOCATION_SEND.matcher(dest);
    return m.matches() && has(a, "DRIVER") && rides.findById(Long.valueOf(m.group(1)))
        .map(r -> r.getDriver() != null && r.getDriver().getUser().getEmail().equals(a.getName())).orElse(false);
  }

  private static boolean has(Authentication a, String role) { return a.getAuthorities().stream().anyMatch(g -> g.getAuthority().equals("ROLE_" + role)); }
}
