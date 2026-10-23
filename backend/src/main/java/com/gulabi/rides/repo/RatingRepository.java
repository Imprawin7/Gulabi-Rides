package com.gulabi.rides.repo;
import com.gulabi.rides.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface RatingRepository extends JpaRepository<Rating,Long> {
  boolean existsByRideId(Long rideId);
}
