package com.gulabi.rides;
import com.gulabi.rides.config.TopicGuard; import com.gulabi.rides.domain.*; import com.gulabi.rides.repo.RideRepository;
import org.junit.jupiter.api.Test; import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication; import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.*; import static org.assertj.core.api.Assertions.assertThat; import static org.mockito.Mockito.*;

class TopicGuardTest {
  RideRepository rides = mock(RideRepository.class); TopicGuard guard = new TopicGuard(rides);
  Authentication as(String email, String role) { return new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))); }

  TopicGuardTest() {
    User rider = new User(); rider.setEmail("rider@x.com");
    User du = new User(); du.setEmail("drv@x.com"); Driver d = new Driver(); d.setUser(du);
    Ride r = new Ride(); r.setId(5L); r.setRider(rider); r.setDriver(d);
    when(rides.findById(5L)).thenReturn(Optional.of(r));
  }

  @Test void riderAndAssignedDriverMaySubscribeToTheirRide() {
    assertThat(guard.canSubscribe(as("rider@x.com", "RIDER"), "/topic/ride/5")).isTrue();
    assertThat(guard.canSubscribe(as("drv@x.com", "DRIVER"), "/topic/ride/5/location")).isTrue();
  }
  @Test void strangersCannotSubscribeToSomeoneElsesRide() {
    assertThat(guard.canSubscribe(as("evil@x.com", "RIDER"), "/topic/ride/5")).isFalse();
    assertThat(guard.canSubscribe(as("evil@x.com", "DRIVER"), "/topic/ride/5")).isFalse();
  }
  @Test void adminTopicsAreAdminOnly() {
    assertThat(guard.canSubscribe(as("rider@x.com", "RIDER"), "/topic/admin/sos")).isFalse();
    assertThat(guard.canSubscribe(as("a@x.com", "ADMIN"), "/topic/admin/sos")).isTrue();
  }
  @Test void requestFeedIsForDriversOnly() {
    assertThat(guard.canSubscribe(as("rider@x.com", "RIDER"), "/user/queue/requests")).isFalse();
    assertThat(guard.canSubscribe(as("drv@x.com", "DRIVER"), "/user/queue/requests")).isTrue();
  }
  @Test void onlyTheAssignedDriverMayPublishLocation() {
    assertThat(guard.canSend(as("drv@x.com", "DRIVER"), "/app/ride/5/location")).isTrue();
    assertThat(guard.canSend(as("other@x.com", "DRIVER"), "/app/ride/5/location")).isFalse();
    assertThat(guard.canSend(as("rider@x.com", "RIDER"), "/app/ride/5/location")).isFalse();
  }
  @Test void unknownDestinationsAreDenied() { assertThat(guard.canSubscribe(as("a@x.com", "ADMIN"), "/topic/other")).isFalse(); }
}
