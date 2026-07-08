package com.multitenant.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/proxy")
public class NominatimProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping(value = "/nominatim", produces = "application/json")
    public ResponseEntity<String> searchNominatim(@RequestParam String q) {
        String url = "https://nominatim.openstreetmap.org/search?q=" + q + "&countrycodes=ro&format=geojson&polygon_geojson=1&email=admin@registru.ro";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "RegistruAgricolApp/1.0");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }
}
