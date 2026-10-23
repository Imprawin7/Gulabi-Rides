package com.gulabi.rides.dto;
import com.gulabi.rides.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal; import java.time.Instant;

public final class Dto {
  private Dto() {}
  public record RegisterRequest(@NotBlank String name, @Email @NotBlank String email, @Size(min = 8) String password,
                                @NotNull Role role, String vehicle, String plate) {}
  public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
  public record GoogleLoginRequest(@NotBlank String idToken, Role role, String vehicle, String plate) {}
  public record ForgotRequest(@Email @NotBlank String email) {}
  public record ResetRequest(@NotBlank String token, @Size(min = 8) String password) {}
  public record TokenResponse(String token, Role role, String name) {}
  public record RideRequest(@NotBlank String pickupName, double pickupLat, double pickupLng,
                            @NotBlank String dropName, double dropLat, double dropLng,
                            @NotNull VehicleType vehicleType, String promoCode) {}
  public record RatingRequest(@Min(1) @Max(5) int stars, @Size(max = 500) String comment) {}
  public record Quote(double km, BigDecimal fare, BigDecimal discount, int etaMinutes) {}
  public record RideView(Long id, RideStatus status, String pickupName, String dropName, BigDecimal fare,
                         double distanceKm, String driverName, String vehicle, String otp, Instant createdAt, Double pickupKm) {
    public static RideView of(Ride r, boolean withOtp) {
      Driver d = r.getDriver();
      return new RideView(r.getId(), r.getStatus(), r.getPickupName(), r.getDropName(), r.getFare(), r.getDistanceKm(),
          d == null ? null : d.getUser().getName(), d == null ? null : d.getVehicle() + " " + d.getPlate(),
          withOtp ? r.getOtp() : null, r.getCreatedAt(), null);
    }
    public RideView withPickupKm(Double km) { return new RideView(id, status, pickupName, dropName, fare, distanceKm, driverName, vehicle, otp, createdAt, km); }
  }
}
