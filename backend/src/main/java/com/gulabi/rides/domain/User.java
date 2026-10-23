package com.gulabi.rides.domain;
import jakarta.persistence.*; import lombok.*; import java.time.Instant;
@Entity @Table(name="users") @Getter @Setter @NoArgsConstructor
public class User {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  private String name;
  @Column(unique=true,nullable=false) private String email;
  @Column(nullable=false) private String passwordHash;
  @Enumerated(EnumType.STRING) private Role role;
  private Instant createdAt=Instant.now();
}
