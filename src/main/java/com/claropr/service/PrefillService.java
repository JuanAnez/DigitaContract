package com.claropr.service;

import com.claropr.adapter.SalesAdapter;
import com.claropr.model.SalesContractPayload;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

@Service
public class PrefillService {

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final SalesAdapter salesAdapter;
    private final RestTemplate restTemplate;

    @Autowired
    public PrefillService(Environment environment, 
                         SalesAdapter salesAdapter) {
        this.environment = environment;
        this.objectMapper = new ObjectMapper();
        this.salesAdapter = salesAdapter;
        this.restTemplate = new RestTemplate();
    }

    // @Cacheable(value = "prefill", key = "#contractUid") // Deshabilitado temporalmente para testing
    public SalesContractPayload getPrefill(String contractUid) throws IOException {
        String mode = environment.getProperty("sales.mode", "mock");
        JsonNode node;

        if ("mock".equalsIgnoreCase(mode)) {
            node = fetchFromMockoon(contractUid);
        } else {
            node = salesAdapter.fetchUnified(contractUid);
        }

        // Convertir a DTO
        return objectMapper.convertValue(node, SalesContractPayload.class);
    }

    /**
     * Búsqueda específica para SIF
     */
    public SalesContractPayload searchNewSaleFromSIF(String contractUid) throws IOException {
        String mode = environment.getProperty("sales.mode", "mock");
        JsonNode node;

        if ("mock".equalsIgnoreCase(mode)) {
            node = fetchFromMockoonSIF(contractUid);
        } else {
            node = salesAdapter.fetchUnified(contractUid);
        }

        return objectMapper.convertValue(node, SalesContractPayload.class);
    }

    /**
     * Búsqueda específica para COPS
     */
    public SalesContractPayload searchNewSaleFromCOPS(String contractUid, String orderNumber) throws IOException {
        String mode = environment.getProperty("sales.mode", "mock");
        JsonNode node;

        if ("mock".equalsIgnoreCase(mode)) {
            node = fetchFromMockoonCOPS(contractUid, orderNumber);
        } else {
            node = salesAdapter.fetchUnified(contractUid);
        }

        return objectMapper.convertValue(node, SalesContractPayload.class);
    }

    /**
     * Búsqueda específica para History
     */
    public SalesContractPayload searchHistory(String contractUid) throws IOException {
        String mode = environment.getProperty("sales.mode", "mock");
        JsonNode node;

        if ("mock".equalsIgnoreCase(mode)) {
            node = fetchFromMockoonHistory(contractUid);
        } else {
            node = salesAdapter.fetchUnified(contractUid);
        }

        return objectMapper.convertValue(node, SalesContractPayload.class);
    }

    private JsonNode fetchFromMockoon(String contractUid) throws IOException {
        String mockoonUrl = environment.getProperty("sales.mockoon.base-url", "http://localhost:3000");
        String endpoint = mockoonUrl + "/api/sif/sales/" + contractUid;
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode responseNode = objectMapper.readTree(response.getBody());
                // El endpoint /api/sif/sales/{contractUid} devuelve la respuesta directamente
                return responseNode;
            } else {
                throw new MockNotFoundException("Mockoon API returned error for contract: " + contractUid);
            }
            
        } catch (Exception e) {
            throw new MockNotFoundException("Failed to fetch data from Mockoon for contract: " + contractUid + ". Error: " + e.getMessage());
        }
    }

    private JsonNode fetchFromMockoonSIF(String contractUid) throws IOException {
        String mockoonUrl = environment.getProperty("sales.mockoon.base-url", "http://localhost:3000");
        String endpoint = mockoonUrl + "/api/sif/sales/" + contractUid;
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readTree(response.getBody());
            } else {
                throw new MockNotFoundException("Mockoon SIF API returned error for contract: " + contractUid);
            }
            
        } catch (Exception e) {
            throw new MockNotFoundException("Failed to fetch SIF data from Mockoon for contract: " + contractUid + ". Error: " + e.getMessage());
        }
    }

    private JsonNode fetchFromMockoonCOPS(String contractUid, String orderNumber) throws IOException {
        String mockoonUrl = environment.getProperty("sales.mockoon.base-url", "http://localhost:3000");
        String endpoint = mockoonUrl + "/api/cops/orders/" + contractUid + (orderNumber != null ? "?orderNumber=" + orderNumber : "");
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readTree(response.getBody());
            } else {
                throw new MockNotFoundException("Mockoon COPS API returned error for contract: " + contractUid);
            }
            
        } catch (Exception e) {
            throw new MockNotFoundException("Failed to fetch COPS data from Mockoon for contract: " + contractUid + ". Error: " + e.getMessage());
        }
    }

    private JsonNode fetchFromMockoonHistory(String contractUid) throws IOException {
        String mockoonUrl = environment.getProperty("sales.mockoon.base-url", "http://localhost:3000");
        String endpoint = mockoonUrl + "/api/history/contracts/" + contractUid;
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readTree(response.getBody());
            } else {
                throw new MockNotFoundException("Mockoon History API returned error for contract: " + contractUid);
            }
            
        } catch (Exception e) {
            throw new MockNotFoundException("Failed to fetch History data from Mockoon for contract: " + contractUid + ". Error: " + e.getMessage());
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class MockNotFoundException extends RuntimeException {
        public MockNotFoundException(String message) {
            super(message);
        }
    }
}
