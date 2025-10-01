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
import com.claropr.service.PrefillService;
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
    
    @Autowired
    private PrefillService prefillService;

    @Override
    public String createContractFromSales(String uid, SalesContractPayload contractData, String pdfUri, String sha256) {
        // Debug: Verificar valores que llegan
        System.out.println("=== ContractServiceImpl.createContractFromSales DEBUG ===");
        System.out.println("UID: " + uid);
        System.out.println("Lob: " + contractData.getLob());
        System.out.println("AccountType: " + contractData.getAccountType());
        System.out.println("Ban: " + contractData.getBan());
        System.out.println("SubscriberNumber: " + contractData.getSubscriberNumber());
        System.out.println("Source: " + contractData.getSource());
        System.out.println("OrderId: " + contractData.getOrderId());
        System.out.println("TemplateId: " + contractData.getTemplateId());
        System.out.println("SaleInfo: " + (contractData.getSaleInfo() != null ? "Present" : "NULL"));
        System.out.println("Seller: " + (contractData.getSaleInfo() != null && contractData.getSaleInfo().getSeller() != null ? "Present" : "NULL"));
        
        // Crear registro del contrato con valores por defecto para evitar NULLs
        ContractRecord contract = new ContractRecord();
        contract.setContractUid(uid);
        contract.setContractType(contractData.getLob() != null ? contractData.getLob() : "MOBILE");
        contract.setAccountType(contractData.getAccountType() != null ? contractData.getAccountType() : "POSTPAID");
        contract.setBanNumber(contractData.getBan() != null ? contractData.getBan() : "N/A");
        contract.setSubscriberNumber(contractData.getSubscriberNumber() != null ? contractData.getSubscriberNumber() : "N/A");
        contract.setSourceSystem(contractData.getSource() != null ? contractData.getSource() : "UNKNOWN");
        contract.setExternalId(contractData.getOrderId() != null ? contractData.getOrderId() : uid);
        contract.setTemplateId(contractData.getTemplateId() != null ? contractData.getTemplateId() : "DEFAULT");
        contract.setVersion(1);
        contract.setStatus("DRAFT");
        contract.setStatusMessage("Contrato creado");
        contract.setFileUri(pdfUri);
        contract.setFileSha256(sha256);
        contract.setCreatedByUserId(
            contractData.getSaleInfo() != null && 
            contractData.getSaleInfo().getSeller() != null && 
            contractData.getSaleInfo().getSeller().getEmployeeId() != null ? 
                contractData.getSaleInfo().getSeller().getEmployeeId() : 
                "SYSTEM"
        );
        
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
    public SalesContractPayload getContractCompleteData(String contractUid) {
        try {
            System.out.println("=== ContractServiceImpl.getContractCompleteData START ===");
            System.out.println("Contract UID: " + contractUid);
            
            // Estrategia de fallback: Primero intentar Mockoon, luego BD
            SalesContractPayload contractData = null;
            
            // 1. Intentar obtener datos desde Mockoon (venta original)
            try {
                contractData = prefillService.getPrefill(contractUid);
                System.out.println("✅ Mockoon data obtained successfully - using original sale data");
                
                // Verificar si el contrato existe en la BD para logging
                try {
                    ContractRecord contract = getContractByUid(contractUid);
                    System.out.println("Database contract found (ID: " + contract.getId() + ") - using Mockoon data as source of truth");
                } catch (Exception dbError) {
                    System.out.println("Contract not found in database - using Mockoon data for new contract");
                }
                
            } catch (Exception mockoonError) {
                System.err.println("⚠️ Mockoon data not available: " + mockoonError.getMessage());
                
                // 2. Fallback: Intentar obtener datos desde la base de datos
                try {
                    System.out.println("🔄 Attempting to get data from database as fallback...");
                    contractData = getContractDataFromDatabase(contractUid);
                    System.out.println("✅ Database data obtained successfully - using database data as fallback");
                    
                } catch (Exception dbError) {
                    System.err.println("❌ Database data also not available: " + dbError.getMessage());
                    throw new RuntimeException("No se pudieron obtener datos del contrato desde Mockoon ni desde la base de datos. Mockoon error: " + mockoonError.getMessage() + ". Database error: " + dbError.getMessage());
                }
            }
            
            System.out.println("=== ContractServiceImpl.getContractCompleteData SUCCESS ===");
            return contractData;
            
        } catch (Exception e) {
            System.err.println("=== ContractServiceImpl.getContractCompleteData ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void logContractAction(String contractUid, String action, String status, String message, String actor) {
        contractDao.logAudit(contractUid, action, status, message, actor);
    }
    
    /**
     * Obtener datos del contrato desde la base de datos como fallback
     * cuando Mockoon no está disponible
     */
    private SalesContractPayload getContractDataFromDatabase(String contractUid) {
        try {
            System.out.println("=== ContractServiceImpl.getContractDataFromDatabase START ===");
            System.out.println("Contract UID: " + contractUid);
            
            // Obtener datos básicos del contrato
            ContractRecord contract = getContractByUid(contractUid);
            System.out.println("Contract found in database: ID=" + contract.getId());
            
            // Obtener partes del contrato (cliente, agente)
            List<ContractParty> parties = contractDao.getContractParties(contract.getId().toString());
            System.out.println("Contract parties found: " + parties.size());
            
            // Construir SalesContractPayload con datos básicos
            SalesContractPayload payload = new SalesContractPayload();
            payload.setSource(contract.getSourceSystem());
            payload.setContractUid(contract.getContractUid());
            payload.setOrderId(contract.getExternalId());
            payload.setBan(contract.getBanNumber());
            payload.setSubscriberNumber(contract.getSubscriberNumber());
            payload.setAccountType(contract.getAccountType());
            payload.setLob(contract.getContractType());
            payload.setTemplateId(contract.getTemplateId());
            
            // Construir customer desde ContractParty
            ContractParty customerParty = parties.stream()
                .filter(party -> "CUSTOMER".equals(party.getRole()))
                .findFirst()
                .orElse(null);
                
            if (customerParty != null) {
                SalesContractPayload.Customer customer = new SalesContractPayload.Customer();
                customer.setFullName(customerParty.getFullName());
                customer.setEmail(customerParty.getEmail());
                customer.setPhone(customerParty.getPhone());
                customer.setIdType("SSN"); // Valor por defecto
                customer.setIdNumber("N/A"); // Valor por defecto
                
                // Dirección por defecto
                SalesContractPayload.Customer.Address address = new SalesContractPayload.Customer.Address();
                address.setLine1("Dirección no disponible");
                address.setCity("Ciudad no disponible");
                address.setState("PR");
                address.setZip("00000");
                address.setCountry("US");
                customer.setBillingAddress(address);
                
                payload.setCustomer(customer);
                System.out.println("Customer data constructed from database");
            }
            
            // Construir saleInfo desde ContractParty (agente)
            ContractParty agentParty = parties.stream()
                .filter(party -> "SALES_AGENT".equals(party.getRole()))
                .findFirst()
                .orElse(null);
                
            if (agentParty != null) {
                SalesContractPayload.SaleInfo saleInfo = new SalesContractPayload.SaleInfo();
                
                // Store por defecto
                SalesContractPayload.SaleInfo.Store store = new SalesContractPayload.SaleInfo.Store();
                store.setCode("R114");
                store.setName("CENTRO ATENCION CLIENTE");
                store.setAddress("Dirección no disponible");
                store.setPhone("787-775-0000");
                saleInfo.setStore(store);
                
                // Seller desde agent party
                SalesContractPayload.SaleInfo.Seller seller = new SalesContractPayload.SaleInfo.Seller();
                seller.setName(agentParty.getFullName());
                seller.setEmployeeId(contract.getCreatedByUserId());
                seller.setEmail(agentParty.getEmail());
                saleInfo.setSeller(seller);
                
                // Fechas por defecto
                saleInfo.setSaleDate(java.time.Instant.now().toString());
                saleInfo.setCloseDate(java.time.LocalDate.now().toString());
                
                payload.setSaleInfo(saleInfo);
                System.out.println("Sale info constructed from database");
            }
            
            // Datos por defecto para equipment, services, totals
            // Estos campos no están disponibles en la BD, así que usamos valores por defecto
            payload.setDevices(new java.util.ArrayList<>());
            
            // PlanAndServices por defecto
            SalesContractPayload.PlanAndServices planAndServices = new SalesContractPayload.PlanAndServices();
            SalesContractPayload.PlanAndServices.Plan plan = new SalesContractPayload.PlanAndServices.Plan();
            plan.setName("Plan mensual postpago");
            plan.setCode("POSTPAGO_STD");
            plan.setBasePrice(new java.math.BigDecimal("50.0"));
            plan.setFeatures(java.util.Arrays.asList("Llamadas", "Texto", "Data básica"));
            planAndServices.setPlan(plan);
            planAndServices.setAddons(new java.util.ArrayList<>());
            payload.setPlanAndServices(planAndServices);
            
            // Totals por defecto
            SalesContractPayload.Totals totals = new SalesContractPayload.Totals();
            totals.setEstimatedMonthly(new java.math.BigDecimal("50.0"));
            SalesContractPayload.Totals.NextBillEstimate nextBill = new SalesContractPayload.Totals.NextBillEstimate();
            nextBill.setEstimatedTotal(new java.math.BigDecimal("50.0"));
            totals.setNextBillEstimate(nextBill);
            payload.setTotals(totals);
            
            // Flags por defecto
            SalesContractPayload.Flags flags = new SalesContractPayload.Flags();
            flags.setPortIn(false);
            flags.setInsurance(false);
            flags.setBundle(false);
            payload.setFlags(flags);
            
            // Legal por defecto
            SalesContractPayload.Legal legal = new SalesContractPayload.Legal();
            legal.setTermsVersion("DEFAULT-2025");
            SalesContractPayload.Legal.Consents consents = new SalesContractPayload.Legal.Consents();
            consents.setElectronicSignature(true);
            consents.setPrivacyNotice(true);
            consents.setCreditCheck(true);
            legal.setConsents(consents);
            payload.setLegal(legal);
            
            System.out.println("=== ContractServiceImpl.getContractDataFromDatabase SUCCESS ===");
            return payload;
            
        } catch (Exception e) {
            System.err.println("=== ContractServiceImpl.getContractDataFromDatabase ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error obteniendo datos del contrato desde la base de datos: " + e.getMessage());
        }
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

            // Subir PDF a Object Storage (MinIO/S3/Oracle) con reintentos
            ObjectStorageService.ObjectStorageResult result = uploadWithRetry(contractUid, pdfBytes);

            if (!result.isSuccess()) {
                // Si es rate limiting, usar fallback temporal
                if (result.getErrorMessage() != null && result.getErrorMessage().contains("reduce your request rate")) {
                    String tempUri = "temp://minio-rate-limited/" + contractUid + ".pdf";
                    System.out.println("⚠️ MinIO rate limited, using temporary URI: " + tempUri);
                    
                    // Actualizar la base de datos con URI temporal
                    contractDao.updateSignedPdfWithObjectStorage(contractUid, tempUri, "temp-sha256", "MINIO_RATE_LIMITED");
                    return tempUri;
                } else {
                    throw new Exception("Error subiendo PDF a Object Storage: " + result.getErrorMessage());
                }
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

    /**
     * Sube un PDF a Object Storage con lógica de reintento para manejar rate limiting
     */
    private ObjectStorageService.ObjectStorageResult uploadWithRetry(String contractUid, byte[] pdfBytes) {
        int maxRetries = 3;
        long baseDelayMs = 1000; // 1 segundo base
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("=== Upload attempt " + attempt + " of " + maxRetries + " ===");
                
                ObjectStorageService.ObjectStorageResult result = objectStorageService.uploadSignedPdf(contractUid, pdfBytes);
                
                if (result.isSuccess()) {
                    System.out.println("✅ Upload successful on attempt " + attempt);
                    return result;
                }
                
                // Si es rate limiting, esperar antes del siguiente intento
                if (result.getErrorMessage() != null && 
                    result.getErrorMessage().contains("reduce your request rate")) {
                    
                    if (attempt < maxRetries) {
                        long delayMs = baseDelayMs * attempt; // Delay exponencial: 1s, 2s, 3s
                        System.out.println("⚠️ Rate limited, waiting " + delayMs + "ms before retry...");
                        Thread.sleep(delayMs);
                    }
                } else {
                    // Si no es rate limiting, no reintentar
                    System.out.println("❌ Upload failed with non-rate-limit error: " + result.getErrorMessage());
                    return result;
                }
                
            } catch (InterruptedException e) {
                System.err.println("❌ Thread interrupted during retry delay");
                Thread.currentThread().interrupt();
                return new ObjectStorageService.ObjectStorageResult(null, null, 0, "ERROR", "Thread interrupted");
            } catch (Exception e) {
                System.err.println("❌ Upload attempt " + attempt + " failed: " + e.getMessage());
                if (attempt == maxRetries) {
                    return new ObjectStorageService.ObjectStorageResult(null, null, 0, "ERROR", e.getMessage());
                }
            }
        }
        
        return new ObjectStorageService.ObjectStorageResult(null, null, 0, "ERROR", "All retry attempts failed");
    }
}
