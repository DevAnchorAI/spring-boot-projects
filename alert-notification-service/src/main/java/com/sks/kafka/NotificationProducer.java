package com.sks.kafka;

import com.sks.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;
    private static final String TOPIC = "notifications";

    public void publish(NotificationRequest request) {
        try {
            // fire-and-forget publish; KafkaTemplate returns a ListenableFuture but we avoid explicit callback types here
            ProducerRecord<String,NotificationRequest > record = new ProducerRecord<>(TOPIC,"notification-msg-key",request);
            kafkaTemplate.send(TOPIC, request);
            log.debug("Published notification request to Kafka topic {}", TOPIC);
        } catch (Exception e) {
            log.error("Exception publishing notification to Kafka: {}", e.getMessage(), e);
            throw e;
        }
    }
}
