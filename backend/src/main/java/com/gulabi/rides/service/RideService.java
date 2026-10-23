package com.gulabi.rides.service;
import com.gulabi.rides.domain.*; import com.gulabi.rides.dto.Dto.*; import com.gulabi.rides.repo.*;
import lombok.RequiredArgsConstructor; import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate; import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.server.ResponseStatusException;
import org.springframework.scheduling.annotation.Scheduled; import java.math.*; import java.security.SecureRandom; import java.time.*; import java.util.*;

@Service @RequiredArgsConstructor
public class RideService {
  private static final SecureRandom RNG = new SecureRandom();
  private final RideRepository rides; private final DriverRepository drivers; private final UserRepository users;
  private final PromoCodeRepository promos; private final RatingRepository ratings; private final SimpMessagingTemplate ws; private final DistanceService distance; private final DispatchService dispatch; private final com.gulabi.rides.events.RideEventPublisher events;
  @org.springframework.beans.factory.annotation.Value("${app.request-expiry-minutes:10}") private int expiryMinutes = 10;

  private static ResponseStatusException err(HttpStatus s, String m) { return new ResponseStatusException(s, m); }

  public Quote quote(double la1, double ln1, double la2, double ln2, VehicleType t, String promo) {
    var leg = distance.between(la1, ln1, la2, ln2);
    double km = leg.km();
    BigDecimal raw = BigDecimal.valueOf(t.base + t.perKm * km), disc = BigDecimal.ZERO;
    if (promo != null && !promo.isBlank())
      disc = promos.findById(promo.trim().toUpperCase()).filter(PromoCode::isActive)
          .map(p -> raw.multiply(BigDecimal.valueOf(p.getPercentOff() / 100.0)).min(p.getMaxDiscount())).orElse(BigDecimal.ZERO);
    return new Quote(km, raw.subtract(disc).setScale(0, RoundingMode.HALF_UP), disc.setScale(0, RoundingMode.HALF_UP), leg.minutes());
  }

  @Transactional
  public RideView request(String email, RideRequest q) {
    User u = users.findByEmail(email).orElseThrow(() -> err(HttpStatus.UNAUTHORIZED, "Unknown user"));
    Quote qt = quote(q.pickupLat(), q.pickupLng(), q.dropLat(), q.dropLng(), q.vehicleType(), q.promoCode());
    Ride r = new Ride(); r.setRider(u); r.setPickupName(q.pickupName()); r.setPickupLat(q.pickupLat()); r.setPickupLng(q.pickupLng());
    r.setDropName(q.dropName()); r.setDropLat(q.dropLat()); r.setDropLng(q.dropLng()); r.setVehicleType(q.vehicleType());
    r.setDistanceKm(qt.km()); r.setFare(qt.fare()); r.setPromoCode(q.promoCode()); r.setStatus(RideStatus.REQUESTED);
    rides.save(r);
    dispatch.notifyNearby(r);
    events.publish(r, "RIDE_REQUESTED");
    return RideView.of(r, true);
  }

  public List<RideView> mine(String email) { return rides.findByRiderEmailOrderByCreatedAtDesc(email).stream().map(r -> RideView.of(r, true)).toList(); }
  public List<RideView> driverHistory(String email) { return rides.findByDriverUserEmailOrderByCreatedAtDesc(email).stream().map(r -> RideView.of(r, false)).toList(); }
  public List<RideView> openRequests(String email) { return dispatch.openFor(email); }

  @Transactional
  public RideView cancel(String email, Long id) {
    Ride r = get(id);
    if (!r.getRider().getEmail().equals(email)) throw err(HttpStatus.FORBIDDEN, "Not your ride");
    if (r.getStatus() != RideStatus.REQUESTED && r.getStatus() != RideStatus.ACCEPTED) throw err(HttpStatus.CONFLICT, "Ride can no longer be cancelled");
    push(r, RideStatus.CANCELLED);
    return RideView.of(r, true);
  }

  /** @Version on Ride makes a double-accept fail with an optimistic-lock error instead of assigning two drivers. */
  @Transactional
  public RideView accept(String driverEmail, Long id) {
    Driver d = driverOf(driverEmail); Ride r = get(id);
    if (!d.isOnline()) throw err(HttpStatus.CONFLICT, "Go online first");
    if (r.getStatus() != RideStatus.REQUESTED) throw err(HttpStatus.CONFLICT, "Ride already taken");
    r.setDriver(d); r.setOtp(String.format("%04d", RNG.nextInt(10000)));
    return push(r, RideStatus.ACCEPTED);
  }

  @Transactional public RideView arrive(String email, Long id) { return push(own(email, id, RideStatus.ACCEPTED), RideStatus.ARRIVED); }
  @Transactional public RideView start(String email, Long id, String otp) {
    Ride r = own(email, id, RideStatus.ARRIVED);
    if (!Objects.equals(r.getOtp(), otp)) throw err(HttpStatus.BAD_REQUEST, "Wrong OTP");
    return push(r, RideStatus.IN_TRIP);
  }
  @Transactional public RideView complete(String email, Long id) { return push(own(email, id, RideStatus.IN_TRIP), RideStatus.COMPLETED); }

  @Transactional
  public void rate(String email, Long id, RatingRequest q) {
    Ride r = get(id);
    if (!r.getRider().getEmail().equals(email)) throw err(HttpStatus.FORBIDDEN, "Not your ride");
    if (r.getStatus() != RideStatus.COMPLETED && r.getStatus() != RideStatus.PAID) throw err(HttpStatus.CONFLICT, "Ride not finished");
    if (ratings.existsByRideId(id)) throw err(HttpStatus.CONFLICT, "Already rated");
    Rating g = new Rating(); g.setRide(r); g.setStars(q.stars()); g.setComment(q.comment()); ratings.save(g);
    Driver d = r.getDriver(); int n = d.getRatingCount();
    d.setRatingAvg((d.getRatingAvg() * n + q.stars()) / (n + 1)); d.setRatingCount(n + 1); drivers.save(d);
  }

  public Map<String, Object> earnings(String email) {
    Driver d = driverOf(email);
    var done = rides.findByDriverUserEmailOrderByCreatedAtDesc(email).stream()
        .filter(r -> r.getStatus() == RideStatus.COMPLETED || r.getStatus() == RideStatus.PAID).toList();
    BigDecimal total = done.stream().map(Ride::getFare).reduce(BigDecimal.ZERO, BigDecimal::add).multiply(new BigDecimal("0.80"));
    return Map.of("trips", done.size(), "earnings", total.setScale(0, RoundingMode.HALF_UP), "rating", Math.round(d.getRatingAvg() * 100) / 100.0);
  }

  /** Requests nobody accepted within the expiry window are cancelled so riders are not left waiting. */
  @Scheduled(fixedDelay = 60_000) @Transactional
  public void expireStale() {
    Instant cutoff = Instant.now().minus(Duration.ofMinutes(expiryMinutes));
    rides.findByStatusAndCreatedAtBefore(RideStatus.REQUESTED, cutoff).forEach(r -> push(r, RideStatus.CANCELLED));
  }

  public Ride paidRideOf(String email, Long id) {
    Ride r = get(id);
    if (!r.getRider().getEmail().equals(email)) throw err(HttpStatus.FORBIDDEN, "Not your ride");
    if (r.getStatus() != RideStatus.PAID) throw err(HttpStatus.CONFLICT, "Receipt is available after payment");
    return r;
  }

  public Driver driverOf(String email) { return drivers.findByUserEmail(email).orElseThrow(() -> err(HttpStatus.FORBIDDEN, "Not a driver")); }
  private Ride get(Long id) { return rides.findById(id).orElseThrow(() -> err(HttpStatus.NOT_FOUND, "Ride not found")); }
  private Ride own(String email, Long id, RideStatus expected) {
    Ride r = get(id);
    if (r.getDriver() == null || !r.getDriver().getUser().getEmail().equals(email)) throw err(HttpStatus.FORBIDDEN, "Not your ride");
    if (r.getStatus() != expected) throw err(HttpStatus.CONFLICT, "Ride is " + r.getStatus() + ", expected " + expected);
    return r;
  }
  private RideView push(Ride r, RideStatus s) {
    r.setStatus(s); rides.save(r);
    events.publish(r, "RIDE_" + s.name());
    ws.convertAndSend("/topic/ride/" + r.getId(), RideView.of(r, false));
    return RideView.of(r, false); // the OTP is only ever shown to the rider
  }
}
