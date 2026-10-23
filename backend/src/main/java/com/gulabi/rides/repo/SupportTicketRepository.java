package com.gulabi.rides.repo;
import com.gulabi.rides.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface SupportTicketRepository extends JpaRepository<SupportTicket,Long> {
  List<SupportTicket> findByUserEmail(String email);
}
