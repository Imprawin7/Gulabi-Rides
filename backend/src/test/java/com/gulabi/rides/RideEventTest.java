package com.gulabi.rides;
import com.gulabi.rides.domain.*; import com.gulabi.rides.events.RideEvent; import org.junit.jupiter.api.Test; import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class RideEventTest {
  @Test void capturesRideStateAndParticipants() {
    User rider = new User(); rider.setEmail("r@x.com");
    User du = new User(); du.setEmail("d@x.com"); Driver d = new Driver(); d.setUser(du);
    Ride r = new Ride(); r.setId(9L); r.setRider(rider); r.setDriver(d); r.setStatus(RideStatus.ACCEPTED); r.setFare(new BigDecimal("120"));
    RideEvent e = RideEvent.from(r, "RIDE_ACCEPTED");
    assertThat(e.rideId()).isEqualTo(9L); assertThat(e.type()).isEqualTo("RIDE_ACCEPTED"); assertThat(e.status()).isEqualTo("ACCEPTED");
    assertThat(e.rider()).isEqualTo("r@x.com"); assertThat(e.driver()).isEqualTo("d@x.com"); assertThat(e.at()).isNotNull();
  }
  @Test void toleratesRidesWithoutADriverYet() {
    User rider = new User(); rider.setEmail("r@x.com"); Ride r = new Ride(); r.setId(1L); r.setRider(rider); r.setStatus(RideStatus.REQUESTED);
    assertThat(RideEvent.from(r, "RIDE_REQUESTED").driver()).isNull();
  }
}
