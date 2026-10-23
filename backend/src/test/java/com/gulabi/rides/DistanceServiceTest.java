package com.gulabi.rides;
import com.gulabi.rides.service.DistanceService; import org.junit.jupiter.api.Test; import static org.assertj.core.api.Assertions.assertThat;

class DistanceServiceTest {
  DistanceService svc = new DistanceService(""); // no API key: straight-line estimate

  @Test void shortCityHopIsAFewKilometres() {
    var leg = svc.between(26.9196, 75.7878, 26.9239, 75.8267); // Jaipur Junction -> Hawa Mahal
    assertThat(leg.km()).isBetween(3.0, 8.0);
    assertThat(leg.minutes()).isGreaterThanOrEqualTo(1);
  }
  @Test void longerTripsTakeLongerAndCostMoreKilometres() {
    var near = svc.between(26.9196, 75.7878, 26.9239, 75.8267);
    var far = svc.between(26.8242, 75.8122, 26.9855, 75.8513); // Airport -> Amer Fort
    assertThat(far.km()).isGreaterThan(near.km());
    assertThat(far.minutes()).isGreaterThan(near.minutes());
  }
  @Test void identicalPointsStillGiveAtLeastOneMinute() { assertThat(svc.between(26.9, 75.8, 26.9, 75.8).minutes()).isEqualTo(1); }
}
