package com.gulabi.rides.web;
import com.gulabi.rides.domain.*; import com.gulabi.rides.dto.Dto.RideView; import com.gulabi.rides.repo.*;
import com.stripe.Stripe; import com.stripe.exception.StripeException; import com.stripe.model.*; import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams; import jakarta.annotation.PostConstruct; import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; import org.springframework.http.*; import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal; import java.security.Principal; import java.util.Map;

@RestController @RequestMapping("/api/payments") @RequiredArgsConstructor
public class PaymentController {
  @Value("${stripe.secret}") private String secret;
  @Value("${stripe.webhook-secret}") private String webhookSecret;
  private final RideRepository rides; private final PaymentRepository payments; private final SimpMessagingTemplate ws; private final com.gulabi.rides.service.MailService mail; private final com.gulabi.rides.events.RideEventPublisher events;
  @PostConstruct void init() { Stripe.apiKey = secret; }

  @PostMapping("/intent/{rideId}") @Transactional
  public Map<String, String> intent(@PathVariable Long rideId, Principal p) throws StripeException {
    Ride r = rides.findById(rideId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!r.getRider().getEmail().equals(p.getName())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    if (r.getStatus() != RideStatus.COMPLETED) throw new ResponseStatusException(HttpStatus.CONFLICT, "Ride is not ready for payment");
    long paise = r.getFare().multiply(BigDecimal.valueOf(100)).longValueExact();
    PaymentIntent pi = PaymentIntent.create(PaymentIntentCreateParams.builder().setAmount(paise).setCurrency("inr")
        .putMetadata("rideId", r.getId().toString())
        .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()).build());
    Payment pay = payments.findByRideId(rideId).orElseGet(Payment::new);
    pay.setRide(r); pay.setAmountPaise(paise); pay.setStripePaymentIntentId(pi.getId()); pay.setStatus("PENDING"); payments.save(pay);
    return Map.of("clientSecret", pi.getClientSecret());
  }

  /** Stripe calls this endpoint; the signature check is what proves the event is genuine. */
  @PostMapping("/webhook") @Transactional
  public ResponseEntity<String> webhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sig) {
    Event e;
    try { e = Webhook.constructEvent(payload, sig, webhookSecret); } catch (Exception ex) { return ResponseEntity.badRequest().body("invalid signature"); }
    if ("payment_intent.succeeded".equals(e.getType())) {
      e.getDataObjectDeserializer().getObject().ifPresent(o -> payments.findByStripePaymentIntentId(((PaymentIntent) o).getId()).ifPresent(pay -> {
        pay.setStatus("SUCCEEDED"); payments.save(pay);
        Ride r = pay.getRide(); r.setStatus(RideStatus.PAID); rides.save(r);
        ws.convertAndSend("/topic/ride/" + r.getId(), RideView.of(r, false));
        mail.sendReceipt(r);
        events.publish(r, "RIDE_PAID");
      }));
    }
    return ResponseEntity.ok("ok");
  }
}
