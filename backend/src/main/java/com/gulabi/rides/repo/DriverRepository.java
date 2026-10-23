package com.gulabi.rides.repo;
import com.gulabi.rides.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface DriverRepository extends JpaRepository<Driver,Long> {
  Optional<Driver> findByUserEmail(String email); long countByOnlineTrue(); List<Driver> findByOnlineTrue();
}
