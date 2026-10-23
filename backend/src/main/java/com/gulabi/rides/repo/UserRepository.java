package com.gulabi.rides.repo;
import com.gulabi.rides.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface UserRepository extends JpaRepository<User,Long> {
  Optional<User> findByEmail(String email); boolean existsByEmail(String email);
}
