package com.gulabi.rides.repo;
import com.gulabi.rides.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface PaymentRepository extends JpaRepository<Payment,Long> {
  Optional<Payment> findByRideId(Long rideId); Optional<Payment> findByStripePaymentIntentId(String id);
}
