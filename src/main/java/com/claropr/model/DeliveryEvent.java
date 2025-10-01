package com.claropr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEvent {
    private String id;
    private String contractId;
    private String channel;
    private String address;
    private String status;
    private String providerMsgId;
    private String errorMessage;
    private Timestamp sentAt;
}

