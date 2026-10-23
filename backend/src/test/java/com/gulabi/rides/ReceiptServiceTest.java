package com.gulabi.rides;
import com.gulabi.rides.domain.*; import com.gulabi.rides.service.ReceiptService; import org.junit.jupiter.api.Test;
import java.math.BigDecimal; import static org.assertj.core.api.Assertions.assertThat;

class ReceiptServiceTest {
  @Test void producesAPdf() {
    User rider = new User(); rider.setName("Asha"); rider.setEmail("asha@x.com");
    Ride r = new Ride(); r.setId(42L); r.setRider(rider); r.setPickupName("Albert Hall"); r.setDropName("Hawa Mahal");
    r.setVehicleType(VehicleType.MINI); r.setDistanceKm(4.2); r.setFare(new BigDecimal("90")); r.setStatus(RideStatus.PAID);
    byte[] pdf = new ReceiptService().pdf(r);
    assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
  }
}
