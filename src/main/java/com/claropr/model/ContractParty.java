package com.claropr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractParty {
    private String id;
    private String contractId;
    private String role;
    private String fullName;
    private String email;
    private String phone;
    private Timestamp createdAt;
}

