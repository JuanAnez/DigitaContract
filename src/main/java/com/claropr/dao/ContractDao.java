package com.claropr.dao;

import com.claropr.model.ContractRecord;
import com.claropr.model.ContractParty;
import com.claropr.model.DeliveryEvent;
import com.claropr.model.AuditLog;

import java.sql.Timestamp;
import java.util.List;

public interface ContractDao {
    
    // Contratos
    void createContract(ContractRecord contract);
    void updateContractStatus(String contractUid, String status, String statusMessage);
    void updateSignedPdf(String contractUid, String signedPdfPath, String sha256);
    void updateSignedPdfWithObjectStorage(String contractUid, String objectUri, String sha256, String storageType);
    void markSigned(String contractUid, String signedPdfUri, String signedPdfSha256,
                   Timestamp customerSignedAt, Timestamp agentSignedAt);
    ContractRecord getContractByUid(String contractUid);
    List<ContractRecord> getContractsByStatus(String status);
    List<ContractRecord> getAllContracts();
    
    // Partes
    void addContractParty(ContractParty party);
    List<ContractParty> getContractParties(String contractId);
    
    // Auditoría
    void logAudit(String contractUid, String action, String status, String message, String actor);
    List<AuditLog> getAuditLog(String contractId);
    
    // Entregas
    void logDelivery(String contractUid, String channel, String address, String status, 
                    String providerMsgId, String errorMessage);
    List<DeliveryEvent> getDeliveryEvents(String contractId);
    
}
