package com.gulabi.rides.domain;
import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal; import java.time.Instant;
@Entity @Table(name="rides") @Getter @Setter @NoArgsConstructor
public class Ride {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Version private Long version;
  @ManyToOne(optional=false) private User rider;
  @ManyToOne private Driver driver;
  private String pickupName, dropName;
  private double pickupLat, pickupLng, dropLat, dropLng, distanceKm;
  @Column(precision=10,scale=2) private BigDecimal fare;
  @Enumerated(EnumType.STRING) private VehicleType vehicleType;
  @Enumerated(EnumType.STRING) private RideStatus status;
  private String otp, promoCode;
  private Instant createdAt=Instant.now();
}
