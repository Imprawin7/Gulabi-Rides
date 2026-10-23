package com.gulabi.rides;
import com.gulabi.rides.domain.*; import com.gulabi.rides.dto.Dto.Quote; import com.gulabi.rides.repo.*; import com.gulabi.rides.service.RideService;
import org.junit.jupiter.api.*; import org.springframework.messaging.simp.SimpMessagingTemplate; import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal; import java.util.*;
import static org.assertj.core.api.Assertions.*; import static org.mockito.Mockito.*;

class RideServiceTest {
  RideRepository rides = mock(RideRepository.class); DriverRepository drivers = mock(DriverRepository.class); UserRepository users = mock(UserRepository.class);
  PromoCodeRepository promos = mock(PromoCodeRepository.class); RatingRepository ratings = mock(RatingRepository.class);
  RideService svc = new RideService(rides, drivers, users, promos, ratings, mock(SimpMessagingTemplate.class), new com.gulabi.rides.service.DistanceService(""), mock(com.gulabi.rides.service.DispatchService.class), mock(com.gulabi.rides.events.RideEventPublisher.class));

  // Jaipur Junction -> Hawa Mahal
  static final double[] A = {26.9196, 75.7878}, B = {26.9239, 75.8267};

  Driver driver(String email, boolean online) {
    User u = new User(); u.setEmail(email); u.setName("Driver"); Driver d = new Driver(); d.setUser(u); d.setOnline(online); d.setId(7L); return d;
  }
  Ride ride(RideStatus s, Driver d) { Ride r = new Ride(); r.setId(1L); r.setStatus(s); r.setDriver(d); return r; }

  @Test void quoteWithoutPromoCostsMoreThanBaseFare() {
    Quote q = svc.quote(A[0], A[1], B[0], B[1], VehicleType.MINI, null);
    assertThat(q.fare()).isGreaterThan(BigDecimal.valueOf(VehicleType.MINI.base));
    assertThat(q.discount()).isEqualByComparingTo("0");
  }

  @Test void promoReducesFareAndRespectsCap() {
    when(promos.findById("WELCOME20")).thenReturn(Optional.of(new PromoCode("WELCOME20", 20, new BigDecimal("100"), true)));
    Quote plain = svc.quote(A[0], A[1], B[0], B[1], VehicleType.SEDAN, null);
    Quote promo = svc.quote(A[0], A[1], B[0], B[1], VehicleType.SEDAN, "welcome20");
    assertThat(promo.discount()).isPositive().isLessThanOrEqualTo(new BigDecimal("100"));
    assertThat(promo.fare().add(promo.discount()).subtract(plain.fare()).abs()).isLessThanOrEqualTo(BigDecimal.ONE);
  }

  @Test void inactivePromoIsIgnored() {
    when(promos.findById("OLD")).thenReturn(Optional.of(new PromoCode("OLD", 50, new BigDecimal("500"), false)));
    assertThat(svc.quote(A[0], A[1], B[0], B[1], VehicleType.MINI, "OLD").discount()).isEqualByComparingTo("0");
  }

  @Test void cannotAcceptARideThatIsAlreadyTaken() {
    Driver d = driver("d@x.com", true);
    when(drivers.findByUserEmail("d@x.com")).thenReturn(Optional.of(d));
    when(rides.findById(1L)).thenReturn(Optional.of(ride(RideStatus.ACCEPTED, driver("other@x.com", true))));
    assertThatThrownBy(() -> svc.accept("d@x.com", 1L)).isInstanceOf(ResponseStatusException.class).hasMessageContaining("409");
  }

  @Test void offlineDriverCannotAccept() {
    when(drivers.findByUserEmail("d@x.com")).thenReturn(Optional.of(driver("d@x.com", false)));
    when(rides.findById(1L)).thenReturn(Optional.of(ride(RideStatus.REQUESTED, null)));
    assertThatThrownBy(() -> svc.accept("d@x.com", 1L)).isInstanceOf(ResponseStatusException.class);
  }

  @Test void wrongOtpDoesNotStartTheTrip() {
    Driver d = driver("d@x.com", true);
    Ride r = ride(RideStatus.ARRIVED, d); r.setOtp("1234");
    when(rides.findById(1L)).thenReturn(Optional.of(r));
    assertThatThrownBy(() -> svc.start("d@x.com", 1L, "0000")).isInstanceOf(ResponseStatusException.class).hasMessageContaining("400");
    assertThat(r.getStatus()).isEqualTo(RideStatus.ARRIVED);
  }

  @Test void correctOtpStartsTheTrip() {
    Driver d = driver("d@x.com", true);
    Ride r = ride(RideStatus.ARRIVED, d); r.setOtp("1234");
    r.setRider(new User());
    when(rides.findById(1L)).thenReturn(Optional.of(r));
    svc.start("d@x.com", 1L, "1234");
    assertThat(r.getStatus()).isEqualTo(RideStatus.IN_TRIP);
  }

  @Test void unclaimedRequestsExpireAndAreCancelled() {
    Ride r = ride(RideStatus.REQUESTED, null); r.setRider(new User());
    when(rides.findByStatusAndCreatedAtBefore(eq(RideStatus.REQUESTED), any())).thenReturn(List.of(r));
    svc.expireStale();
    assertThat(r.getStatus()).isEqualTo(RideStatus.CANCELLED);
  }
}
