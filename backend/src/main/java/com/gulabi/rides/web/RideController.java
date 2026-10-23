package com.gulabi.rides.web;
import com.gulabi.rides.domain.VehicleType; import com.gulabi.rides.dto.Dto.*; import com.gulabi.rides.service.RideService;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*;
import com.gulabi.rides.service.ReceiptService; import org.springframework.http.*; import java.security.Principal; import java.util.List;

@RestController @RequestMapping("/api/rides") @RequiredArgsConstructor
public class RideController {
  private final RideService svc; private final ReceiptService receipts;
  @GetMapping("/quote") public Quote quote(@RequestParam double pLat, @RequestParam double pLng, @RequestParam double dLat, @RequestParam double dLng,
                                            @RequestParam VehicleType type, @RequestParam(required = false) String promo) { return svc.quote(pLat, pLng, dLat, dLng, type, promo); }
  @PostMapping public RideView request(@Valid @RequestBody RideRequest q, Principal p) { return svc.request(p.getName(), q); }
  @GetMapping("/mine") public List<RideView> mine(Principal p) { return svc.mine(p.getName()); }
  @PostMapping("/{id}/cancel") public RideView cancel(@PathVariable Long id, Principal p) { return svc.cancel(p.getName(), id); }
  @PostMapping("/{id}/rating") public void rate(@PathVariable Long id, @Valid @RequestBody RatingRequest q, Principal p) { svc.rate(p.getName(), id, q); }
  @GetMapping("/{id}/receipt") public ResponseEntity<byte[]> receipt(@PathVariable Long id, Principal p) {
    byte[] pdf = receipts.pdf(svc.paidRideOf(p.getName(), id));
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt-" + id + ".pdf").body(pdf);
  }
}
