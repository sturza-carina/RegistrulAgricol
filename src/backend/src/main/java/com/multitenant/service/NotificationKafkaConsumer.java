package com.multitenant.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class NotificationKafkaConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public NotificationKafkaConsumer(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "contracte-expirare", groupId = "portal-notificari-group")
    public void consumeNotification(String message) {
        try {
            System.out.println("[NotificationKafkaConsumer] Received Kafka message: " + message);
            Map<String, String> payload = objectMapper.readValue(message, new TypeReference<Map<String, String>>() {});
            String cnp = payload.get("cnp");
            String mesaj = payload.get("mesaj");

            if (cnp != null && mesaj != null) {
                String destination = "/topic/notificari/" + cnp;
                messagingTemplate.convertAndSend(destination, payload);
                System.out.println("[NotificationKafkaConsumer] Successfully broadcasted notification via WebSocket to destination: " + destination);
            }
        } catch (Exception e) {
            System.err.println("[NotificationKafkaConsumer] Error processing Kafka message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "notificari-succesiuni", groupId = "portal-notificari-group")
    public void consumeSuccesiune(String message) {
        try {
            System.out.println("[NotificationKafkaConsumer] Received Kafka message for succesiune: " + message);
            Map<String, Object> payload = objectMapper.readValue(message, new TypeReference<Map<String, Object>>() {});
            String mesaj = (String) payload.get("mesaj");

            if (mesaj != null) {
                // Broadcast către toți utilizatorii pe topicul general de notificări
                String destination = "/topic/notificari/general";
                messagingTemplate.convertAndSend(destination, payload);
                System.out.println("[NotificationKafkaConsumer] Broadcasted succesiune notification via WebSocket to: " + destination);
            }
        } catch (Exception e) {
            System.err.println("[NotificationKafkaConsumer] Error processing succesiune Kafka message: " + e.getMessage());
        }
    }
}
