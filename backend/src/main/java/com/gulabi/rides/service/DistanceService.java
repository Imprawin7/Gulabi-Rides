package com.gulabi.rides.service;
import com.fasterxml.jackson.databind.JsonNode; import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value; import org.springframework.cache.annotation.Cacheable; import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient; import java.io.Serializable;

/** Road distance and travel time. Uses the Google Distance Matrix API when a key is set, otherwise a straight-line estimate. */
@Service @Slf4j
public class DistanceService {
  /** `estimated` legs are never cached, so a temporary API failure is not remembered. */
  public record Leg(double km, int minutes, boolean estimated) implements Serializable {}
  private final String apiKey; private final RestClient http = RestClient.create();

  public DistanceService(@Value("${app.google-maps-key:}") String apiKey) { this.apiKey = apiKey; }

  /** Cached in the "distance" cache: in memory by default, Redis when CACHE_TYPE=redis. */
  @Cacheable(cacheNames = "distance", unless = "#result.estimated()",
      key = "T(java.lang.String).format('%.4f,%.4f>%.4f,%.4f', #la1, #ln1, #la2, #ln2)")
  public Leg between(double la1, double ln1, double la2, double ln2) {
    if (apiKey == null || apiKey.isBlank()) return estimate(la1, ln1, la2, ln2);
    try {
      JsonNode root = http.get().uri("https://maps.googleapis.com/maps/api/distancematrix/json?origins={o}&destinations={d}&mode=driving&key={k}",
          la1 + "," + ln1, la2 + "," + ln2, apiKey).retrieve().body(JsonNode.class);
      JsonNode el = root.path("rows").path(0).path("elements").path(0);
      if ("OK".equals(el.path("status").asText()))
        return new Leg(Math.round(el.path("distance").path("value").asDouble() / 100.0) / 10.0,
            (int) Math.max(1, Math.round(el.path("duration").path("value").asDouble() / 60.0)), false);
      log.warn("Distance Matrix returned {} / {}", root.path("status").asText(), el.path("status").asText());
    } catch (Exception e) { log.warn("Distance Matrix call failed, using estimate: {}", e.getMessage()); }
    return estimate(la1, ln1, la2, ln2);
  }

  static Leg estimate(double la1, double ln1, double la2, double ln2) {
    double km = haversine(la1, ln1, la2, ln2) * 1.3; // rough road factor
    return new Leg(Math.round(km * 10) / 10.0, (int) Math.max(1, Math.round(km / 25 * 60)), true); // ~25 km/h city speed
  }

  static double haversine(double la1, double ln1, double la2, double ln2) {
    double R = 6371, dLa = Math.toRadians(la2 - la1), dLn = Math.toRadians(ln2 - ln1);
    double a = Math.pow(Math.sin(dLa / 2), 2) + Math.cos(Math.toRadians(la1)) * Math.cos(Math.toRadians(la2)) * Math.pow(Math.sin(dLn / 2), 2);
    return 2 * R * Math.asin(Math.sqrt(a));
  }
}
