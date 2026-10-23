package com.gulabi.rides;
import com.gulabi.rides.domain.*; import com.gulabi.rides.repo.*; import com.gulabi.rides.web.RankingController; import org.junit.jupiter.api.Test;
import java.util.List; import static org.assertj.core.api.Assertions.assertThat; import static org.mockito.Mockito.*;

class RankingControllerTest {
  Driver driver(Long id, String name, double avg, int count) {
    User u = new User(); u.setName(name); u.setEmail(name.toLowerCase() + "@x.com");
    Driver d = new Driver(); d.setId(id); d.setUser(u); d.setVehicle("Car"); d.setRatingAvg(avg); d.setRatingCount(count); return d;
  }

  @Test void manyGoodRatingsOutrankASingleFiveStar() {
    DriverRepository drivers = mock(DriverRepository.class); RideRepository rides = mock(RideRepository.class);
    when(drivers.findAll()).thenReturn(List.of(driver(1L, "Lucky", 5.0, 1), driver(2L, "Steady", 4.8, 100)));
    var out = new RankingController(drivers, rides).ranking(() -> "lucky@x.com");
    assertThat(out.get(0).name()).isEqualTo("Steady"); assertThat(out.get(0).rank()).isEqualTo(1);
    assertThat(out.get(1).you()).isTrue(); assertThat(out.get(0).you()).isFalse();
  }
}
