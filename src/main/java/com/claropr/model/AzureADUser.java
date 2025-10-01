package com.claropr.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AzureADUser {
    private String id;
    
    @JsonProperty("displayName")
    private String displayName;
    
    private String mail;
    
    @JsonProperty("userPrincipalName")
    private String userPrincipalName;
    
    @JsonProperty("givenName")
    private String givenName;
    
    private String surname;
}
