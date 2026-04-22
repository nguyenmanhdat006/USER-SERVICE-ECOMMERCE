package com.ecommerce.userservice.event;

import com.ecommerce.userservice.config.KafkaProducerProperties;
import com.ecommerce.userservice.dto.event.UserRegisteredEvent;
import com.ecommerce.userservice.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProducerProperties kafkaProducerProperties;

    public void publishAfterCommit(User user) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish(user);
                }
            });
            return;
        }

        publish(user);
    }

    private void publish(User user) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("USER_REGISTERED")
                .timestamp(LocalDateTime.now())
                .userId(user.getId().toString())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .build();

        String userRegisteredTopic = kafkaProducerProperties.getUserRegisteredTopic();

        kafkaTemplate.send(userRegisteredTopic, event.getUserId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Published USER_REGISTERED event: eventId={}, userId={}, topic={}, partition={}, offset={}",
                                event.getEventId(),
                                event.getUserId(),
                                userRegisteredTopic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to publish USER_REGISTERED event: eventId={}, userId={}",
                                event.getEventId(),
                                event.getUserId(),
                                ex);
                    }
                });
    }
}

