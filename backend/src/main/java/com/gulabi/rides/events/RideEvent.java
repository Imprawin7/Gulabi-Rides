package com.gulabi.rides.events;
import com.gulabi.rides.domain.Ride; import java.math.BigDecimal; import java.time.Instant;

/** Ride lifecycle event published to the "ride-events" topic when Kafka is enabled. */
public record RideEvent(Long rideId, String type, String status, String rider, String driver, BigDecimal fare, Instant at) {
  public static RideEvent from(Ride r, String type) {
    return new RideEvent(r.getId(), type, r.getStatus() == null ? null : r.getStatus().name(),
        r.getRider() == null ? null : r.getRider().getEmail(),
        r.getDriver() == null ? null : r.getDriver().getUser().getEmail(), r.getFare(), Instant.now());
  }
}
