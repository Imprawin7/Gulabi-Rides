package com.gulabi.rides.events;
import lombok.extern.slf4j.Slf4j; import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener; import org.springframework.stereotype.Component;

/** Example downstream consumer. Replace the log line with analytics, notifications or a data-warehouse sink. */
@Component @ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true") @Slf4j
public class RideEventAuditListener {
  @KafkaListener(topics = KafkaRideEventPublisher.TOPIC, groupId = "gulabi-audit")
  public void onEvent(String json) { log.info("ride-event {}", json); }
}
