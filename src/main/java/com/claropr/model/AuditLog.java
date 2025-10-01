package com.claropr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    private String id;
    private String contractId;
    private String actorUserId;
    private String action;
    private String status;
    private String message;
    private Timestamp at;
}

