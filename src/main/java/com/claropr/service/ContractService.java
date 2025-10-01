package com.claropr.service;

import com.claropr.model.ContractRecord;
import com.claropr.model.ContractParty;
import com.claropr.model.DeliveryEvent;
import com.claropr.model.AuditLog;
import com.claropr.model.SalesContractPayload;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface ContractService {
    
    // Crear contrato desde SalesContractPayload
    String createContractFromSales(String uid, SalesContractPayload contractData, String pdfUri, String sha256);
    
    // Estados del contrato
    void updateContractStatus(String contractUid, String status, String statusMessage);
    void markContractSigned(String contractUid, String signedPdfUri, String sha256, 
                           Timestamp customerSignedAt, Timestamp agentSignedAt);
    
    // Consultas
    ContractRecord getContractByUid(String contractUid);
    SalesContractPayload getContractDataByUid(String contractUid);
    SalesContractPayload getContractCompleteData(String contractUid);
    List<ContractRecord> getContractsByStatus(String status);
    List<ContractRecord> getAllContracts();
    List<AuditLog> getContractAuditLog(String contractUid);
    List<DeliveryEvent> getContractDeliveries(String contractUid);
    
    // Obtener firmas guardadas
    Map<String, String> getContractSignatures(String contractUid);
    
    
    // PDF firmado
    String saveSignedPdf(String contractUid, byte[] pdfBytes) throws Exception;
    byte[] getSignedPdf(String contractUid) throws Exception;
    
    // Auditoría y entregas
    void logContractAction(String contractUid, String action, String status, String message, String actor);
    void logContractDelivery(String contractUid, String channel, String address, String status, 
                            String providerMsgId, String errorMessage);
}
