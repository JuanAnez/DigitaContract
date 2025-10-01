package com.claropr.service;

import com.claropr.model.AzureADToken;
import com.claropr.model.AzureADUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MockAzureADService {
    
    @Value("${azure.activedirectory.authorization-url}")
    private String authorizationUrl;
    
    @Value("${azure.activedirectory.token-url}")
    private String tokenUrl;
    
    @Value("${azure.activedirectory.user-info-url}")
    private String userInfoUrl;
    
    @Value("${azure.activedirectory.client-id}")
    private String clientId;
    
    @Value("${azure.activedirectory.redirect-uri}")
    private String redirectUri;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public String buildAuthUrl() {
        String authUrl = authorizationUrl + "?" +
               "client_id=" + clientId +
               "&response_type=code" +
               "&redirect_uri=" + redirectUri +
               "&scope=openid profile email" +
               "&response_mode=query";
        
        System.out.println("Building Azure AD auth URL: " + authUrl);
        return authUrl;
    }
    
    public AzureADToken exchangeCodeForToken(String code) {
        System.out.println("Exchanging code for token: " + code);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        String body = "code=" + code + 
                     "&client_id=" + clientId + 
                     "&client_secret=test" + 
                     "&grant_type=authorization_code";
        
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<AzureADToken> response = restTemplate.postForEntity(tokenUrl, entity, AzureADToken.class);
            System.out.println("Token exchange successful: " + response.getBody());
            return response.getBody();
        } catch (Exception e) {
            System.out.println("Token exchange failed: " + e.getMessage());
            throw new RuntimeException("Failed to exchange code for token", e);
        }
    }
    
    public AzureADUser getUserInfo(AzureADToken token) {
        System.out.println("Getting user info with token: " + token.getAccessToken());
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.getAccessToken());
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<AzureADUser> response = restTemplate.exchange(
                userInfoUrl, 
                HttpMethod.GET, 
                entity, 
                AzureADUser.class
            );
            System.out.println("User info retrieved: " + response.getBody());
            return response.getBody();
        } catch (Exception e) {
            System.out.println("Failed to get user info: " + e.getMessage());
            throw new RuntimeException("Failed to get user info", e);
        }
    }
}
