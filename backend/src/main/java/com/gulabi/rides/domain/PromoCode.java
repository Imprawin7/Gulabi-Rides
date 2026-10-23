package com.gulabi.rides.domain;
import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal;
@Entity @Table(name="promo_codes") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PromoCode {
  @Id private String code;
  private int percentOff;
  private BigDecimal maxDiscount;
  private boolean active;
}
