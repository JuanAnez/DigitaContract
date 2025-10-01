package com.claropr.service.impl;

import com.claropr.dao.ContractDao;
import com.claropr.model.ContractRecord;
import com.claropr.model.ContractParty;
import com.claropr.model.DeliveryEvent;
import com.claropr.model.AuditLog;
import com.claropr.model.SalesContractPayload;
import com.claropr.service.ContractService;
import com.claropr.service.PdfStorageService;
import com.claropr.service.ObjectStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractDao contractDao;

    @Autowired
    private PdfStorageService pdfStorageService;
    
    @Autowired
    private ObjectStorageService objectStorageService;

    @Override
    public String createContractFromSales(String uid, SalesContractPayload contractData, String pdfUri, String sha256) {
        // Crear registro del contrato
        ContractRecord contract = new ContractRecord();
        contract.setContractUid(uid);
        contract.setContractType(contractData.getLob());
        contract.setAccountType(contractData.getAccountType());
        contract.setBanNumber(contractData.getBan());
        contract.setSubscriberNumber(contractData.getSubscriberNumber());
        contract.setSourceSystem(contractData.getSource());
        contract.setExternalId(contractData.getOrderId());
        contract.setTemplateId(contractData.getTemplateId());
        contract.setVersion(1);
        contract.setStatus("DRAFT");
        contract.setStatusMessage("Contrato creado");
        contract.setFileUri(pdfUri);
        contract.setFileSha256(sha256);
        contract.setCreatedByUserId(contractData.getSaleInfo().getSeller().getEmployeeId());
        
        contractDao.createContract(contract);
        
        System.out.println("=== ContractServiceImpl.createContractFromSales ===");
        System.out.println("Contract ID after creation: " + contract.getId());
        
        // Agregar partes del contrato
        ContractParty customer = new ContractParty();
        customer.setContractId(contract.getId());
        customer.setRole("CUSTOMER");
        customer.setFullName(contractData.getCustomer().getFullName());
        customer.setEmail(contractData.getCustomer().getEmail());
        customer.setPhone(contractData.getCustomer().getPhone());
        
        System.out.println("Adding customer party: " + customer.getFullName() + " with contract ID: " + customer.getContractId());
        contractDao.addContractParty(customer);
        
        ContractParty agent = new ContractParty();
        agent.setContractId(contract.getId());
        agent.setRole("SALES_AGENT");
        agent.setFullName(contractData.getSaleInfo().getSeller().getName());
        agent.setEmail(contractData.getSaleInfo().getSeller().getEmail());
        agent.setPhone(""); // No hay campo phone en Seller
        
        System.out.println("Adding agent party: " + agent.getFullName() + " with contract ID: " + agent.getContractId());
        contractDao.addContractParty(agent);
        
        // Log de auditoría
        System.out.println("Adding audit log for contract: " + uid);
        contractDao.logAudit(uid, "CREATE", "OK", "Contrato creado desde sistema de ventas", 
                           contractData.getSaleInfo().getSeller().getEmployeeId());
        
        return uid;
    }

    @Override
    public void updateContractStatus(String contractUid, String status, String statusMessage) {
        contractDao.updateContractStatus(contractUid, status, statusMessage);
    }

    @Override
    public void markContractSigned(String contractUid, String signedPdfUri, String sha256, 
                                 Timestamp customerSignedAt, Timestamp agentSignedAt) {
        contractDao.markSigned(contractUid, signedPdfUri, sha256, customerSignedAt, agentSignedAt);
    }

    @Override
    public ContractRecord getContractByUid(String contractUid) {
        return contractDao.getContractByUid(contractUid);
    }

    @Override
    public List<ContractRecord> getContractsByStatus(String status) {
        return contractDao.getContractsByStatus(status);
    }

    @Override
    public List<ContractRecord> getAllContracts() {
        return contractDao.getAllContracts();
    }

    @Override
    public List<AuditLog> getContractAuditLog(String contractUid) {
        ContractRecord contract = getContractByUid(contractUid);
        return contractDao.getAuditLog(contract.getId());
    }

    @Override
    public List<DeliveryEvent> getContractDeliveries(String contractUid) {
        ContractRecord contract = getContractByUid(contractUid);
        return contractDao.getDeliveryEvents(contract.getId());
    }

    @Override
    public SalesContractPayload getContractDataByUid(String contractUid) {
        ContractRecord contract = getContractByUid(contractUid);
        List<ContractParty> parties = contractDao.getContractParties(contract.getId());
        
        // Crear un objeto básico con la información disponible
        SalesContractPayload payload = new SalesContractPayload();
        
        // Información básica del contrato
        payload.setLob(contract.getContractType());
        payload.setAccountType(contract.getAccountType());
        payload.setBan(contract.getBanNumber());
        payload.setSubscriberNumber(contract.getSubscriberNumber());
        payload.setSource(contract.getSourceSystem());
        payload.setOrderId(contract.getExternalId());
        payload.setTemplateId(contract.getTemplateId());
        
        // Información de las partes
        for (ContractParty party : parties) {
            if ("CUSTOMER".equals(party.getRole())) {
                SalesContractPayload.Customer customer = new SalesContractPayload.Customer();
                customer.setFullName(party.getFullName());
                customer.setEmail(party.getEmail());
                customer.setPhone(party.getPhone());
                payload.setCustomer(customer);
            } else if ("SALES_AGENT".equals(party.getRole())) {
                SalesContractPayload.SaleInfo saleInfo = new SalesContractPayload.SaleInfo();
                SalesContractPayload.SaleInfo.Seller seller = new SalesContractPayload.SaleInfo.Seller();
                seller.setName(party.getFullName());
                seller.setEmail(party.getEmail());
                seller.setEmployeeId(contract.getCreatedByUserId());
                saleInfo.setSeller(seller);
                payload.setSaleInfo(saleInfo);
            }
        }
        
        return payload;
    }

    @Override
    public void logContractAction(String contractUid, String action, String status, String message, String actor) {
        contractDao.logAudit(contractUid, action, status, message, actor);
    }

    @Override
    public void logContractDelivery(String contractUid, String channel, String address, String status, 
                                   String providerMsgId, String errorMessage) {
        contractDao.logDelivery(contractUid, channel, address, status, providerMsgId, errorMessage);
    }

    @Override
    public Map<String, String> getContractSignatures(String contractUid) {
        // Por ahora retornamos un mapa vacío ya que las firmas se manejan en el frontend
        // En el futuro se podrían guardar las firmas en la base de datos
        return new java.util.HashMap<>();
    }

    /**
     * Guarda un PDF firmado para un contrato existente usando Object Storage
     */
    public String saveSignedPdf(String contractUid, byte[] pdfBytes) throws Exception {
        try {
            System.out.println("=== ContractServiceImpl.saveSignedPdf START ===");
            System.out.println("Contract UID: " + contractUid);
            System.out.println("PDF Size: " + pdfBytes.length + " bytes");

            // Subir PDF a Object Storage (MinIO/S3/Oracle)
            ObjectStorageService.ObjectStorageResult result = objectStorageService.uploadSignedPdf(contractUid, pdfBytes);

            if (!result.isSuccess()) {
                throw new Exception("Error subiendo PDF a Object Storage: " + result.getErrorMessage());
            }

            // Actualizar la base de datos con la información del Object Storage
            contractDao.updateSignedPdfWithObjectStorage(contractUid, result.getObjectUri(), result.getSha256(), "MINIO");

            // Log de auditoría
            contractDao.logAudit(contractUid, "SAVE_SIGNED_PDF_OBJECT_STORAGE", "OK", 
                "PDF firmado guardado en Object Storage: " + result.getObjectUri(), "system");

            System.out.println("=== ContractServiceImpl.saveSignedPdf SUCCESS ===");
            System.out.println("Object URI: " + result.getObjectUri());
            System.out.println("SHA256: " + result.getSha256());
            
            return result.getObjectUri();

        } catch (Exception e) {
            System.err.println("=== ContractServiceImpl.saveSignedPdf ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Obtiene el PDF firmado de un contrato desde Object Storage
     */
    public byte[] getSignedPdf(String contractUid) throws Exception {
        try {
            System.out.println("=== ContractServiceImpl.getSignedPdf START ===");
            System.out.println("Contract UID: " + contractUid);

            // Obtener información del contrato
            ContractRecord contract = contractDao.getContractByUid(contractUid);
            if (contract == null) {
                throw new Exception("Contrato no encontrado: " + contractUid);
            }

            if (contract.getSignedPdfUri() == null) {
                throw new Exception("PDF firmado no encontrado para el contrato: " + contractUid);
            }

            // Descargar PDF desde Object Storage
            byte[] pdfBytes = objectStorageService.downloadSignedPdf(contract.getSignedPdfUri());

            System.out.println("PDF firmado obtenido exitosamente:");
            System.out.println("- URI: " + contract.getSignedPdfUri());
            System.out.println("- Tamaño: " + pdfBytes.length + " bytes");
            System.out.println("- Storage Type: " + contract.getPdfStorageType());

            return pdfBytes;

        } catch (Exception e) {
            System.err.println("=== ContractServiceImpl.getSignedPdf ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
