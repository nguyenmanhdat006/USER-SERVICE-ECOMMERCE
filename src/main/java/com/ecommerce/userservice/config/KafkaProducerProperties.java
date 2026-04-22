package com.ecommerce.userservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.kafka.producer")
@Data
public class KafkaProducerProperties {

    /**
     * Topic for user registration success events.
     */
    private String userRegisteredTopic = "user.registered";
}

