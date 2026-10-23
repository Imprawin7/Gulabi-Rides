package com.gulabi.rides.domain;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="ratings") @Getter @Setter @NoArgsConstructor
public class Rating {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @OneToOne(optional=false) private Ride ride;
  private int stars; @Column(length = 500) private String comment;
}
