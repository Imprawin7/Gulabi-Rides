package com.gulabi.rides.repo;
import com.gulabi.rides.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.time.Instant; import java.util.*;
public interface RideRepository extends JpaRepository<Ride,Long> {
  List<Ride> findByRiderEmailOrderByCreatedAtDesc(String email);
  List<Ride> findByDriverUserEmailOrderByCreatedAtDesc(String email);
  List<Ride> findByStatusOrderByCreatedAtDesc(RideStatus status);
  List<Ride> findTop50ByOrderByCreatedAtDesc();
  List<Ride> findByStatusAndCreatedAtBefore(RideStatus status, Instant before);
  long countByDriverIdAndStatusIn(Long driverId, Collection<RideStatus> statuses);
}
