package com.gulabi.rides.events;
import org.apache.kafka.clients.admin.NewTopic; import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*; import org.springframework.kafka.config.TopicBuilder;

@Configuration @ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaConfig {
  @Bean NewTopic rideEventsTopic() { return TopicBuilder.name(KafkaRideEventPublisher.TOPIC).partitions(3).replicas(1).build(); }
}
