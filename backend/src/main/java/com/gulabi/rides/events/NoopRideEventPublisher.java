package com.gulabi.rides.events;
import com.gulabi.rides.domain.Ride; import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; import org.springframework.stereotype.Component;
@Component @ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoopRideEventPublisher implements RideEventPublisher { @Override public void publish(Ride ride, String type) { } }
