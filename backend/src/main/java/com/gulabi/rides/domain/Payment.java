package com.gulabi.rides.domain;
import jakarta.persistence.*; import lombok.*; import java.time.Instant;
@Entity @Table(name="payments") @Getter @Setter @NoArgsConstructor
public class Payment {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @OneToOne(optional=false) private Ride ride;
  private long amountPaise;
  @Column(unique=true) private String stripePaymentIntentId;
  private String status;
  private Instant createdAt=Instant.now();
}
