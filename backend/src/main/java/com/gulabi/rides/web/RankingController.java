package com.gulabi.rides.web;
import com.gulabi.rides.domain.*; import com.gulabi.rides.repo.*; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*;
import java.security.Principal; import java.util.*;

@RestController @RequestMapping("/api/drivers") @RequiredArgsConstructor
public class RankingController {
  private static final double PRIOR = 4.5, WEIGHT = 5; // new drivers start near 4.5 and move as ratings come in
  public record Entry(int rank, String name, String vehicle, double rating, int ratings, long trips, double score, boolean you) {}
  private final DriverRepository drivers; private final RideRepository rides;

  /** Top 20 drivers by weighted rating, so one 5-star review does not outrank a hundred 4.8s. */
  @GetMapping("/ranking")
  public List<Entry> ranking(Principal p) {
    List<Driver> top = drivers.findAll().stream()
        .sorted(Comparator.<Driver>comparingDouble(RankingController::score).reversed().thenComparing(d -> d.getUser().getName())).limit(20).toList();
    List<Entry> out = new ArrayList<>();
    for (int i = 0; i < top.size(); i++) {
      Driver d = top.get(i);
      out.add(new Entry(i + 1, d.getUser().getName(), d.getVehicle(), round(d.getRatingAvg()), d.getRatingCount(),
          rides.countByDriverIdAndStatusIn(d.getId(), List.of(RideStatus.COMPLETED, RideStatus.PAID)), round(score(d)), d.getUser().getEmail().equals(p.getName())));
    }
    return out;
  }

  static double score(Driver d) { return (d.getRatingAvg() * d.getRatingCount() + PRIOR * WEIGHT) / (d.getRatingCount() + WEIGHT); }
  private static double round(double v) { return Math.round(v * 100) / 100.0; }
}
