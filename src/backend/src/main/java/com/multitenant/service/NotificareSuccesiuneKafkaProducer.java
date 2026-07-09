package com.multitenant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificareSuccesiuneKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "notificari-succesiuni";

    public NotificareSuccesiuneKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void trimiteEvenimentDeces(Long defunctId, String numeDefunct, String numeNotar, String numarAdresa) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("defunctId", defunctId);
            payload.put("numeDefunct", numeDefunct);
            payload.put("numeNotar", numeNotar);
            payload.put("numarAdresaOficiala", numarAdresa);
            payload.put("mesaj", "Defunctul " + numeDefunct + " a fost marcat ca decedat. S-a înregistrat notificarea de succesiune cu adresa nr. " + numarAdresa + " către " + numeNotar + ".");
            
            String jsonMessage = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(TOPIC, String.valueOf(defunctId), jsonMessage);
            System.out.println("[NotificareSuccesiuneKafkaProducer] Sent Kafka message to " + TOPIC + " for Defunct ID " + defunctId + ": " + jsonMessage);
        } catch (Exception e) {
            System.err.println("[NotificareSuccesiuneKafkaProducer] Eroare la trimiterea mesajului Kafka pentru succesiune: " + e.getMessage());
        }
    }
}
