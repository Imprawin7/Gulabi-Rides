package com.gulabi.rides.events;
import com.fasterxml.jackson.databind.ObjectMapper; import com.gulabi.rides.domain.Ride; import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async; import org.springframework.stereotype.Component;

/** Publishes asynchronously and never throws: a Kafka outage must not stop rides from progressing. */
@Component @ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true") @RequiredArgsConstructor @Slf4j
public class KafkaRideEventPublisher implements RideEventPublisher {
  public static final String TOPIC = "ride-events";
  private final KafkaTemplate<String, String> kafka; private final ObjectMapper mapper;

  @Override @Async
  public void publish(Ride ride, String type) {
    try {
      RideEvent e = RideEvent.from(ride, type);
      kafka.send(TOPIC, String.valueOf(e.rideId()), mapper.writeValueAsString(e)); // keyed by ride id: one ride's events stay in order
    } catch (Exception ex) { log.warn("Could not publish {} for ride {}: {}", type, ride.getId(), ex.getMessage()); }
  }
}
