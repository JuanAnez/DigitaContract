package com.claropr.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SalesAdapter {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${sales.sif.base-url:}")
    private String sifBaseUrl;

    @Value("${sales.sif.api-key:}")
    private String sifApiKey;

    @Value("${sales.cops.base-url:}")
    private String copsBaseUrl;

    @Value("${sales.cops.api-key:}")
    private String copsApiKey;

    public JsonNode fetchUnified(String contractUid) {
        ObjectNode unified = objectMapper.createObjectNode();

        try {
            // TODO: Implementar llamadas reales a SIF y COPS
            // Ejemplo de estructura para SIF:
            // ResponseEntity<JsonNode> sifResponse = restTemplate.exchange(
            //     sifBaseUrl + "/sales/{uid}", 
            //     HttpMethod.GET, 
            //     createEntityWithKey(sifApiKey), 
            //     JsonNode.class, 
            //     contractUid
            // );

            // Ejemplo de estructura para COPS:
            // ResponseEntity<JsonNode> copsResponse = restTemplate.exchange(
            //     copsBaseUrl + "/orders/{uid}", 
            //     HttpMethod.GET, 
            //     createEntityWithKey(copsApiKey), 
            //     JsonNode.class, 
            //     contractUid
            // );

            // TODO: Mapear respuestas de SIF/COPS al formato unificado
            // unified.put("source", "SIF");
            // unified.put("contractUid", contractUid);
            // ... completar mapeo según estructura del JSON único ...

            // Por ahora, retornar estructura básica para testing
            unified.put("source", "MOCK");
            unified.put("contractUid", contractUid);
            unified.put("orderId", "ORD-" + contractUid);
            unified.put("accountType", "POSTPAID");
            unified.put("lob", "MOBILE");
            unified.put("language", "es");

            return unified;

        } catch (Exception e) {
            throw new RuntimeException("Error fetching unified data for contract: " + contractUid, e);
        }
    }

    private HttpEntity<Void> createEntityWithKey(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");
        return new HttpEntity<>(headers);
    }
}
