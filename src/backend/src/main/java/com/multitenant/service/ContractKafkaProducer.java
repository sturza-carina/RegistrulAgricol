package com.multitenant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class ContractKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "contracte-expirare";

    public ContractKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void trimiteAlertaExpirare(String cnp, String mesaj) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("cnp", cnp);
            payload.put("mesaj", mesaj);
            String jsonMessage = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(TOPIC, cnp, jsonMessage);
            System.out.println("Sent Kafka message to " + TOPIC + " for CNP " + cnp + ": " + jsonMessage);
        } catch (Exception e) {
            throw new RuntimeException("Eroare la trimiterea mesajului Kafka", e);
        }
    }
}
