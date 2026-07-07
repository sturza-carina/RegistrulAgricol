package com.multitenant.service.signnow;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Client subțire peste API-ul SignNow (https://docs.signnow.com), folosit pentru
 * semnarea electronică la distanță a contractelor de utilizare: încarcă PDF-ul
 * generat, trimite o invitație "free form" către semnatar și descarcă documentul
 * final după ce acesta a fost semnat pe platforma SignNow.
 */
@Component
public class SignNowClient {

    @Value("${app.signnow.base-url}")
    private String baseUrl;

    @Value("${app.signnow.basic-token}")
    private String basicToken;

    @Value("${app.signnow.username}")
    private String username;

    @Value("${app.signnow.password}")
    private String password;

    private final RestTemplate restTemplate = new RestTemplate();

    private volatile String cachedAccessToken;
    private volatile Instant tokenExpiry;

    private synchronized String getAccessToken() {
        if (cachedAccessToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry)) {
            return cachedAccessToken;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basicToken);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);
        body.add("scope", "*");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        Map<String, Object> respBody = exchangeForMap(baseUrl + "/oauth2/token", HttpMethod.POST, request);
        if (respBody == null || respBody.get("access_token") == null) {
            throw new IllegalStateException("Autentificarea la SignNow a eșuat: răspuns invalid");
        }

        cachedAccessToken = (String) respBody.get("access_token");
        int expiresIn = Integer.parseInt(String.valueOf(respBody.getOrDefault("expires_in", "3600")));
        tokenExpiry = Instant.now().plusSeconds(Math.max(expiresIn - 60, 60));
        return cachedAccessToken;
    }

    private Map<String, Object> exchangeForMap(String url, HttpMethod method, HttpEntity<?> request) {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, method, request, new ParameterizedTypeReference<Map<String, Object>>() {});
        return response.getBody();
    }

    /** Încarcă PDF-ul nesemnat în SignNow și întoarce id-ul documentului creat. */
    public String uploadDocument(byte[] pdfBytes, String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ByteArrayResource fileResource = new ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        Map<String, Object> respBody = exchangeForMap(baseUrl + "/document", HttpMethod.POST, request);
        if (respBody == null || respBody.get("id") == null) {
            throw new IllegalStateException("Încărcarea documentului în SignNow a eșuat");
        }
        return (String) respBody.get("id");
    }

    /**
     * Trimite o invitație "free form" - semnatarul poate aplica semnătura oriunde în document.
     * Planurile SignNow free/trial nu permit personalizarea subiectului/mesajului invitației
     * (eroare "Upgrade your subscription plan..."), așa că trimitem doar to/from.
     */
    public void sendFreeFormInvite(String documentId, String signerEmail) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "to", signerEmail,
                "from", username
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(baseUrl + "/document/" + documentId + "/invite", request, String.class);
    }

    /** Verifică statusul curent al documentului (ex. câmpul "status" / lista de semnături din răspuns). */
    public Map<String, Object> getDocumentDetails(String documentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return exchangeForMap(baseUrl + "/document/" + documentId, HttpMethod.GET, request);
    }

    /** True dacă documentul are cel puțin o semnătură aplicată (câmp "signatures" nevid în răspunsul SignNow). */
    public boolean isSigned(Map<String, Object> documentDetails) {
        Object signatures = documentDetails.get("signatures");
        return signatures instanceof List<?> list && !list.isEmpty();
    }

    /** Descarcă documentul final (cu semnătura aplicată), ca un singur PDF "aplatizat". */
    public byte[] downloadSignedDocument(String documentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(
                baseUrl + "/document/" + documentId + "/download?type=collapsed",
                HttpMethod.GET, request, byte[].class);
        return response.getBody();
    }
}
