package com.gulabi.rides.domain;
import jakarta.persistence.*; import lombok.*; import java.time.Instant;
@Entity @Table(name="support_tickets") @Getter @Setter @NoArgsConstructor
public class SupportTicket {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) private User user;
  @ManyToOne private Ride ride;
  private String subject; @Column(length=2000) private String body;
  private String status="OPEN";
  private Instant createdAt=Instant.now();
}
