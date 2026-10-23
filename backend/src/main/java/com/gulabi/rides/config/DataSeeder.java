package com.gulabi.rides.config;
import com.gulabi.rides.domain.*; import com.gulabi.rides.repo.*; import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder; import java.math.BigDecimal;

@Configuration
public class DataSeeder {
  @Bean CommandLineRunner seed(PromoCodeRepository promos, UserRepository users, PasswordEncoder enc, @Value("${app.admin-password}") String adminPw) {
    return args -> {
      if (!promos.existsById("WELCOME20")) promos.save(new PromoCode("WELCOME20", 20, new BigDecimal("100"), true));
      if (!users.existsByEmail("admin@gulabi.dev")) {
        User a = new User(); a.setName("Admin"); a.setEmail("admin@gulabi.dev"); a.setPasswordHash(enc.encode(adminPw)); a.setRole(Role.ADMIN); users.save(a);
      }
    };
  }
}
