package com.gulabi.rides.service;
import com.gulabi.rides.domain.Ride; import jakarta.mail.internet.MimeMessage; import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider; import org.springframework.beans.factory.annotation.Value; import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.*; import org.springframework.scheduling.annotation.Async; import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor @Slf4j
public class MailService {
  private final ObjectProvider<JavaMailSender> sender; private final ReceiptService receipts;
  @Value("${app.mail-from}") private String from;
  @Value("${app.production:false}") private boolean production;

  /** Emails the PDF receipt. Does nothing (just logs) until spring.mail.host is configured. */
  @Async
  public void sendReceipt(Ride r) {
    JavaMailSender s = sender.getIfAvailable();
    if (s == null) { log.info("Mail not configured; skipping receipt email for ride {}", r.getId()); return; }
    try {
      MimeMessage m = s.createMimeMessage();
      MimeMessageHelper h = new MimeMessageHelper(m, true);
      h.setFrom(from); h.setTo(r.getRider().getEmail()); h.setSubject("Your Gulabi Rides receipt #" + r.getId());
      h.setText("Hi " + r.getRider().getName() + ",\n\nThanks for riding with us. Your receipt is attached.\n\nGulabi Rides");
      h.addAttachment("receipt-" + r.getId() + ".pdf", new ByteArrayResource(receipts.pdf(r)), "application/pdf");
      s.send(m);
    } catch (Exception e) { log.warn("Could not send receipt for ride {}: {}", r.getId(), e.getMessage()); }
  }

  /** Emails the reset link. Without a mail server it logs the link (development only) so the flow can still be tried locally. */
  @Async
  public void sendPasswordReset(String to, String name, String link) {
    JavaMailSender s = sender.getIfAvailable();
    if (s == null) { if (!production) log.info("Mail not configured. Password reset link for {}: {}", to, link); return; }
    try {
      MimeMessage m = s.createMimeMessage();
      MimeMessageHelper h = new MimeMessageHelper(m, false);
      h.setFrom(from); h.setTo(to); h.setSubject("Reset your Gulabi Rides password");
      h.setText("Hi " + name + ",\n\nUse this link to choose a new password (valid for 30 minutes):\n" + link + "\n\nIf you did not ask for this, you can ignore this email.");
      s.send(m);
    } catch (Exception e) { log.warn("Could not send password reset email: {}", e.getMessage()); }
  }
}
