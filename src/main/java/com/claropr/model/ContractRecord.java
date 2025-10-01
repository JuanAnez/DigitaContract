package com.claropr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractRecord {
    private String id;
    private String contractUid;
    private String contractType;
    private String accountType;
    private String banNumber;
    private String subscriberNumber;
    private String sourceSystem;
    private String externalId;
    private String templateId;
    private Integer version;
    private String status;
    private String statusMessage;
    private String fileUri;
    private String fileSha256;
    private String signedPdfUri;
    private String signedPdfSha256;
    private String signedPdfPath;
    private String pdfStorageType;
    private Timestamp pdfGeneratedAt;
    private Timestamp customerSignedAt;
    private Timestamp agentSignedAt;
    private String createdByUserId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
