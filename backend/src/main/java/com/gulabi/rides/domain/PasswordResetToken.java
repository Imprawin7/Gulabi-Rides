package com.gulabi.rides.domain;
import jakarta.persistence.*; import lombok.*; import java.time.Instant;
@Entity @Table(name="password_reset_tokens") @Getter @Setter @NoArgsConstructor
public class PasswordResetToken {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) private User user;
  @Column(length=64, unique=true, nullable=false) private String tokenHash;
  @Column(nullable=false) private Instant expiresAt;
  private boolean used;
}
