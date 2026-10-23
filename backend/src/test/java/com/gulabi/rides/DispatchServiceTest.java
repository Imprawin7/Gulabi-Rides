package com.gulabi.rides;
import com.gulabi.rides.domain.*; import com.gulabi.rides.dto.Dto.RideView; import com.gulabi.rides.repo.*; import com.gulabi.rides.service.DispatchService;
import org.junit.jupiter.api.*; import org.springframework.messaging.simp.SimpMessagingTemplate; import org.springframework.test.util.ReflectionTestUtils;
import java.time.Instant; import java.util.*;
import static org.assertj.core.api.Assertions.assertThat; import static org.mockito.ArgumentMatchers.*; import static org.mockito.Mockito.*;

class DispatchServiceTest {
  DriverRepository drivers = mock(DriverRepository.class); RideRepository rides = mock(RideRepository.class); SimpMessagingTemplate ws = mock(SimpMessagingTemplate.class);
  DispatchService svc = new DispatchService(drivers, rides, ws);
  Ride ride; Driver near, far, unlocated, stale;

  Driver driver(String email, Double lat, Double lng, Instant at) {
    User u = new User(); u.setEmail(email); Driver d = new Driver(); d.setUser(u); d.setOnline(true); d.setLat(lat); d.setLng(lng); d.setLocationAt(at); return d;
  }

  @BeforeEach void setUp() {
    ReflectionTestUtils.setField(svc, "radiusKm", 8.0);
    ride = new Ride(); ride.setId(1L); ride.setStatus(RideStatus.REQUESTED); ride.setPickupLat(26.9196); ride.setPickupLng(75.7878); // Jaipur Junction
    near = driver("near@x.com", 26.9115, 75.8195, Instant.now());            // Albert Hall, ~3 km
    far = driver("far@x.com", 26.8242, 75.8122, Instant.now());              // Airport, ~11 km
    unlocated = driver("nogps@x.com", null, null, null);
    stale = driver("stale@x.com", 26.8242, 75.8122, Instant.now().minusSeconds(600)); // old fix: treated as unknown
    when(drivers.findByOnlineTrue()).thenReturn(List.of(near, far, unlocated, stale));
  }

  @Test void requestGoesToNearbyAndUnlocatedDriversOnly() {
    svc.notifyNearby(ride);
    verify(ws).convertAndSendToUser(eq("near@x.com"), eq("/queue/requests"), any(RideView.class));
    verify(ws).convertAndSendToUser(eq("nogps@x.com"), eq("/queue/requests"), any(RideView.class));
    verify(ws).convertAndSendToUser(eq("stale@x.com"), eq("/queue/requests"), any(RideView.class));
    verify(ws, never()).convertAndSendToUser(eq("far@x.com"), anyString(), any());
  }

  @Test void openRequestsAreFilteredByDistanceAndShowPickupDistance() {
    when(rides.findByStatusOrderByCreatedAtDesc(RideStatus.REQUESTED)).thenReturn(List.of(ride));
    when(drivers.findByUserEmail("near@x.com")).thenReturn(Optional.of(near));
    when(drivers.findByUserEmail("far@x.com")).thenReturn(Optional.of(far));
    var forNear = svc.openFor("near@x.com");
    assertThat(forNear).hasSize(1);
    assertThat(forNear.get(0).pickupKm()).isBetween(1.0, 6.0);
    assertThat(svc.openFor("far@x.com")).isEmpty();
  }

  @Test void updateLocationStoresPositionAndTime() {
    when(drivers.findByUserEmail("near@x.com")).thenReturn(Optional.of(near));
    svc.updateLocation("near@x.com", 26.9, 75.8);
    assertThat(near.getLat()).isEqualTo(26.9); assertThat(near.getLocationAt()).isNotNull();
    verify(drivers).save(near);
  }
}
