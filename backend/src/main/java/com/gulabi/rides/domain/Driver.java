package com.gulabi.rides.domain;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="drivers") @Getter @Setter @NoArgsConstructor
public class Driver {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @OneToOne(optional=false) private User user;
  private String vehicle, plate;
  private boolean online;
  private Double lat, lng; private java.time.Instant locationAt;
  private double ratingAvg=5.0; private int ratingCount=0;
}
