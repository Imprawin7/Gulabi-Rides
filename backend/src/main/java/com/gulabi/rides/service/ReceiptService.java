package com.gulabi.rides.service;
import com.gulabi.rides.domain.Ride;
import com.itextpdf.kernel.pdf.*; import com.itextpdf.layout.Document; import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*; import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream; import java.time.ZoneId; import java.time.format.DateTimeFormatter;

@Service
public class ReceiptService {
  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").withZone(ZoneId.of("Asia/Kolkata"));

  /** Builds a one-page PDF receipt. Amounts use "INR" because the built-in PDF font has no rupee glyph. */
  public byte[] pdf(Ride r) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PdfDocument pdf = new PdfDocument(new PdfWriter(out));
    try (Document doc = new Document(pdf)) {
      doc.add(new Paragraph("Gulabi Rides").setBold().setFontSize(22));
      doc.add(new Paragraph("Receipt for ride #" + r.getId()).setFontSize(12).setMarginBottom(16));
      Table t = new Table(UnitValue.createPercentArray(new float[]{35, 65})).useAllAvailableWidth();
      row(t, "Date", FMT.format(r.getCreatedAt()));
      row(t, "Rider", r.getRider().getName());
      row(t, "Driver", r.getDriver() == null ? "-" : r.getDriver().getUser().getName() + " (" + r.getDriver().getPlate() + ")");
      row(t, "Pickup", r.getPickupName());
      row(t, "Drop-off", r.getDropName());
      row(t, "Vehicle", r.getVehicleType().name());
      row(t, "Distance", r.getDistanceKm() + " km");
      if (r.getPromoCode() != null && !r.getPromoCode().isBlank()) row(t, "Promo code", r.getPromoCode().toUpperCase());
      row(t, "Total paid", "INR " + r.getFare().toPlainString());
      doc.add(t);
      doc.add(new Paragraph("Thank you for riding with us.").setMarginTop(20).setFontSize(10));
    }
    return out.toByteArray();
  }

  private static void row(Table t, String k, String v) {
    t.addCell(new Cell().add(new Paragraph(k).setBold()).setBorder(Border.NO_BORDER));
    t.addCell(new Cell().add(new Paragraph(v == null ? "-" : v)).setBorder(Border.NO_BORDER));
  }
}
