package com.gulabi.rides.service;
import com.gulabi.rides.domain.*; import com.gulabi.rides.dto.Dto.RideView; import com.gulabi.rides.repo.*;
import lombok.RequiredArgsConstructor; import org.springframework.beans.factory.annotation.Value; import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate; import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.server.ResponseStatusException;
import java.time.*; import java.util.List;

/**
 * Decides which drivers see a ride request. A driver whose recent location is more than the dispatch radius from the pickup is skipped.
 * A driver with no recent location (GPS off, app just opened) still sees everything, so requests are never lost.
 */
@Service @RequiredArgsConstructor
public class DispatchService {
  private static final Duration FRESH = Duration.ofMinutes(2);
  private final DriverRepository drivers; private final RideRepository rides; private final SimpMessagingTemplate ws;
  @Value("${app.dispatch-radius-km:8}") private double radiusKm = 8;

  @Transactional
  public void updateLocation(String email, double lat, double lng) {
    Driver d = driverOf(email);
    d.setLat(lat); d.setLng(lng); d.setLocationAt(Instant.now()); drivers.save(d);
  }

  /** Pushes the request to each eligible online driver's private queue (/user/queue/requests). */
  public void notifyNearby(Ride r) {
    for (Driver d : drivers.findByOnlineTrue())
      if (inRange(d, r)) ws.convertAndSendToUser(d.getUser().getEmail(), "/queue/requests", RideView.of(r, false).withPickupKm(pickupKm(d, r)));
  }

  /** Open requests this driver should see, nearest information included when the driver's location is known. */
  public List<RideView> openFor(String email) {
    Driver d = driverOf(email);
    return rides.findByStatusOrderByCreatedAtDesc(RideStatus.REQUESTED).stream().filter(r -> inRange(d, r))
        .map(r -> RideView.of(r, false).withPickupKm(pickupKm(d, r))).toList();
  }

  private Driver driverOf(String email) { return drivers.findByUserEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a driver")); }
  private boolean located(Driver d) { return d.getLat() != null && d.getLng() != null && d.getLocationAt() != null && d.getLocationAt().isAfter(Instant.now().minus(FRESH)); }
  private double km(Driver d, Ride r) { return DistanceService.haversine(d.getLat(), d.getLng(), r.getPickupLat(), r.getPickupLng()); }
  private boolean inRange(Driver d, Ride r) { return !located(d) || km(d, r) <= radiusKm; }
  private Double pickupKm(Driver d, Ride r) { return located(d) ? Math.round(km(d, r) * 10) / 10.0 : null; }
}
