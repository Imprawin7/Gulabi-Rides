package com.gulabi.rides.web;
import com.gulabi.rides.dto.Dto.RideView; import com.gulabi.rides.repo.DriverRepository; import com.gulabi.rides.service.*; import jakarta.validation.Valid; import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.*;

@RestController @RequestMapping("/api/driver") @RequiredArgsConstructor
public class DriverController {
  public record Loc(@DecimalMin("-90") @DecimalMax("90") double lat, @DecimalMin("-180") @DecimalMax("180") double lng) {}
  private final RideService svc; private final DriverRepository drivers; private final DispatchService dispatch;
  @PostMapping("/online") public Map<String, Boolean> online(@RequestParam boolean value, Principal p) {
    var d = svc.driverOf(p.getName()); d.setOnline(value); drivers.save(d); return Map.of("online", value); }
  @GetMapping("/requests") public List<RideView> requests(Principal p) { return svc.openRequests(p.getName()); }
  @PostMapping("/location") public void location(@Valid @RequestBody Loc l, Principal p) { dispatch.updateLocation(p.getName(), l.lat(), l.lng()); }
  @PostMapping("/rides/{id}/accept") public RideView accept(@PathVariable Long id, Principal p) { return svc.accept(p.getName(), id); }
  @PostMapping("/rides/{id}/arrive") public RideView arrive(@PathVariable Long id, Principal p) { return svc.arrive(p.getName(), id); }
  @PostMapping("/rides/{id}/start") public RideView start(@PathVariable Long id, @RequestParam String otp, Principal p) { return svc.start(p.getName(), id, otp); }
  @PostMapping("/rides/{id}/complete") public RideView complete(@PathVariable Long id, Principal p) { return svc.complete(p.getName(), id); }
  @GetMapping("/history") public List<RideView> history(Principal p) { return svc.driverHistory(p.getName()); }
  @GetMapping("/earnings") public Map<String, Object> earnings(Principal p) { return svc.earnings(p.getName()); }
}
