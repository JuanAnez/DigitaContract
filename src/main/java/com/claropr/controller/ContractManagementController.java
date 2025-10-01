package com.claropr.controller;

import com.claropr.model.ApiResponseDTO;
import com.claropr.model.ContractRecord;
import com.claropr.model.AuditLog;
import com.claropr.model.DeliveryEvent;
import com.claropr.model.SalesContractPayload;
import com.claropr.service.ContractService;
import com.claropr.service.ContractPdfService;
import com.claropr.service.ContractEmailService;
import com.claropr.service.PrefillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
public class ContractManagementController {

    @Autowired
    private ContractService contractService;
    
    @Autowired
    private ContractPdfService contractPdfService;
    
    @Autowired
    private ContractEmailService contractEmailService;
    
    @Autowired
    private PrefillService prefillService;

    @GetMapping("/{uid}")
    public ApiResponseDTO<ContractRecord> getContract(@PathVariable String uid) {
        try {
            ContractRecord contract = contractService.getContractByUid(uid);
            return ApiResponseDTO.create(200, "Success", contract);
        } catch (Exception e) {
            return ApiResponseDTO.create(500, "Error retrieving contract: " + e.getMessage(), null);
        }
    }

    @GetMapping("/status/{status}")
    public ApiResponseDTO<List<ContractRecord>> getContractsByStatus(@PathVariable String status) {
        try {
            List<ContractRecord> contracts = contractService.getContractsByStatus(status);
            return ApiResponseDTO.create(200, "Success", contracts);
        } catch (Exception e) {
            return ApiResponseDTO.create(500, "Error retrieving contracts: " + e.getMessage(), null);
        }
    }

    @GetMapping("/list")
    public ApiResponseDTO<List<ContractRecord>> getAllContracts() {
        try {
            List<ContractRecord> contracts = contractService.getAllContracts();
            return ApiResponseDTO.create(200, "Success", contracts);
        } catch (Exception e) {
            return ApiResponseDTO.create(500, "Error retrieving contracts: " + e.getMessage(), null);
        }
    }

    @GetMapping("/{uid}/audit")
    public ApiResponseDTO<List<AuditLog>> getContractAuditLog(@PathVariable String uid) {
        try {
            List<AuditLog> auditLog = contractService.getContractAuditLog(uid);
            return ApiResponseDTO.create(200, "Success", auditLog);
        } catch (Exception e) {
            return ApiResponseDTO.create(500, "Error retrieving audit log: " + e.getMessage(), null);
        }
    }

    @GetMapping("/{uid}/deliveries")
    public ApiResponseDTO<List<DeliveryEvent>> getContractDeliveries(@PathVariable String uid) {
        try {
            List<DeliveryEvent> deliveries = contractService.getContractDeliveries(uid);
            return ApiResponseDTO.create(200, "Success", deliveries);
        } catch (Exception e) {
            return ApiResponseDTO.create(500, "Error retrieving deliveries: " + e.getMessage(), null);
        }
    }

    @GetMapping("/{uid}/signatures")
    public ApiResponseDTO<Map<String, String>> getContractSignatures(@PathVariable String uid) {
        try {
            Map<String, String> signatures = contractService.getContractSignatures(uid);
            return ApiResponseDTO.create(200, "Success", signatures);
        } catch (Exception e) {
            return ApiResponseDTO.create(500, "Error retrieving signatures: " + e.getMessage(), null);
        }
    }

    @PostMapping("/{uid}/save-signed-pdf")
    public ApiResponseDTO<String> saveSignedPdf(@PathVariable String uid, @RequestBody byte[] pdfBytes) {
        try {
            String filePath = contractService.saveSignedPdf(uid, pdfBytes);
            return ApiResponseDTO.create(200, "PDF firmado guardado exitosamente", filePath);
        } catch (Exception e) {
            return ApiResponseDTO.create(500, "Error guardando PDF firmado: " + e.getMessage(), null);
        }
    }

    @GetMapping("/{uid}/signed-pdf")
    public ResponseEntity<byte[]> getSignedPdf(@PathVariable String uid) {
        try {
            byte[] pdfBytes = contractService.getSignedPdf(uid);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "signed-contract-" + uid + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{uid}/status")
    public ApiResponseDTO<String> updateContractStatus(@PathVariable String uid, 
                                                      @RequestParam String status, 
                                                      @RequestParam(required = false) String message) {
        try {
            contractService.updateContractStatus(uid, status, message);
            return ApiResponseDTO.create(200, "Contract status updated successfully", null);
        } catch (Exception e) {
            return ApiResponseDTO.create(500, "Error updating contract status: " + e.getMessage(), null);
        }
    }

    @PostMapping("/{uid}/generate-pdf")
    public ResponseEntity<byte[]> generateContractPdf(@PathVariable String uid, 
                                                     @RequestBody Map<String, String> signatures) {
        try {
            byte[] pdfBytes = contractPdfService.generateContractPdf(uid, signatures);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "contract-" + uid + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/{uid}/generate-and-send")
    public ApiResponseDTO<String> generateAndSendContract(@PathVariable String uid, 
                                                          @RequestBody Map<String, String> signatures) {
        try {
            // Obtener datos del contrato desde el sistema de ventas (Mockoon)
            SalesContractPayload contractData = prefillService.getPrefill(uid);
            
            // Generar PDF
            byte[] pdfBytes = contractPdfService.generateContractPdf(contractData, 
                signatures.get("customerSignature"), signatures.get("consultantSignature"));
            
            // Crear contrato en la base de datos
            String contractId = contractService.createContractFromSales(uid, contractData, 
                "contract-" + uid + ".pdf", "sha256-hash-placeholder");
            
            // Guardar PDF firmado
            try {
                String signedPdfPath = contractService.saveSignedPdf(uid, pdfBytes);
                System.out.println("PDF firmado guardado en: " + signedPdfPath);
            } catch (Exception saveError) {
                System.err.println("Error guardando PDF firmado: " + saveError.getMessage());
                saveError.printStackTrace();
            }
            
            // Intentar enviar por email (no crítico si falla)
            try {
                contractEmailService.sendContractEmail(contractData, pdfBytes, 
                    signatures.get("customerSignature"), signatures.get("consultantSignature"));
                
                // Actualizar estado del contrato
                contractService.updateContractStatus(uid, "SIGNED", "Contract signed and sent via email");
                
                return ApiResponseDTO.create(200, "Contract generated and sent successfully", null);
            } catch (Exception emailError) {
                // Si el email falla, solo logear el error pero continuar
                System.err.println("Error sending email: " + emailError.getMessage());
                emailError.printStackTrace();
                
                // Actualizar estado del contrato sin email
                contractService.updateContractStatus(uid, "SIGNED", "Contract signed but email failed: " + emailError.getMessage());
                
                return ApiResponseDTO.create(200, "Contract generated successfully (email failed)", null);
            }
        } catch (Exception e) {
            return ApiResponseDTO.create(500, "Error generating and sending contract: " + e.getMessage(), null);
        }
    }
}
