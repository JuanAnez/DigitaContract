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

    @GetMapping("/{uid}/complete")
    public ApiResponseDTO<SalesContractPayload> getContractComplete(@PathVariable String uid) {
        try {
            System.out.println("=== ContractManagementController.getContractComplete ===");
            System.out.println("Getting complete contract data for UID: " + uid);
            
            // Obtener datos completos del contrato desde la base de datos
            SalesContractPayload contractData = contractService.getContractCompleteData(uid);
            
            return ApiResponseDTO.create(200, "Success", contractData);
        } catch (Exception e) {
            System.err.println("Error getting complete contract data: " + e.getMessage());
            e.printStackTrace();
            return ApiResponseDTO.create(500, "Error retrieving complete contract data: " + e.getMessage(), null);
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

    @GetMapping("/list/paginated")
    public ApiResponseDTO<Map<String, Object>> getContractsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        try {
            List<ContractRecord> allContracts = contractService.getAllContracts();
            
            // Apply search filter
            if (search != null && !search.trim().isEmpty()) {
                allContracts = allContracts.stream()
                    .filter(contract -> 
                        contract.getContractUid().toLowerCase().contains(search.toLowerCase())
                    )
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Apply status filter
            if (status != null && !status.trim().isEmpty() && !status.equals("all")) {
                allContracts = allContracts.stream()
                    .filter(contract -> contract.getStatus().equalsIgnoreCase(status))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Calculate pagination
            int totalItems = allContracts.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalItems);
            
            List<ContractRecord> paginatedContracts = allContracts.subList(startIndex, endIndex);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("contracts", paginatedContracts);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);
            response.put("pageSize", size);
            
            return ApiResponseDTO.create(200, "Success", response);
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
    public ResponseEntity<byte[]> generateAndSendContract(@PathVariable String uid, 
                                                          @RequestBody Map<String, String> signatures) {
        try {
            System.out.println("=== ContractManagementController.generateAndSendContract START ===");
            System.out.println("Contract UID: " + uid);
            System.out.println("Customer signature present: " + (signatures.get("customerSignature") != null));
            System.out.println("Consultant signature present: " + (signatures.get("consultantSignature") != null));
            
            // Obtener datos del contrato desde la base de datos (datos reales)
            SalesContractPayload contractData = contractService.getContractCompleteData(uid);
            System.out.println("Contract data obtained successfully from database");
            
            // Generar PDF con las firmas
            byte[] pdfBytes = contractPdfService.generateContractPdf(contractData, 
                signatures.get("customerSignature"), signatures.get("consultantSignature"));
            System.out.println("PDF generated successfully, size: " + pdfBytes.length + " bytes");
            
            // Verificar si el contrato existe, si no existe, crearlo
            try {
                ContractRecord existingContract = contractService.getContractByUid(uid);
                System.out.println("✅ Contract already exists in database: " + existingContract.getId());
            } catch (Exception getError) {
                // Contrato no existe, crearlo
                try {
                    contractService.createContractFromSales(uid, contractData, 
                        "contract-" + uid + ".pdf", "sha256-hash-placeholder");
                    System.out.println("✅ Contract created in database");
                } catch (Exception createError) {
                    System.err.println("❌ Error creating contract in database: " + createError.getMessage());
                    createError.printStackTrace();
                    // Continuar aunque falle la creación
                }
            }
            
            // Guardar PDF firmado en MinIO y actualizar URI en base de datos
            String signedPdfUri = contractService.saveSignedPdf(uid, pdfBytes);
            System.out.println("✅ PDF firmado procesado: " + signedPdfUri);
            
            // Intentar enviar por email (no crítico si falla)
            try {
                contractEmailService.sendContractEmail(contractData, pdfBytes, 
                    signatures.get("customerSignature"), signatures.get("consultantSignature"));
                
                // Actualizar estado del contrato
                contractService.updateContractStatus(uid, "SIGNED", "Contract signed and sent via email");
                
                System.out.println("✅ Email enviado exitosamente");
                
                // Devolver el PDF como bytes
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", "contract-" + uid + "-signed.pdf");
                
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(pdfBytes);
                        
            } catch (Exception emailError) {
                // Si el email falla, solo logear el error pero continuar
                System.err.println("⚠️ Error sending email: " + emailError.getMessage());
                emailError.printStackTrace();
                
                // Actualizar estado del contrato sin email
                contractService.updateContractStatus(uid, "SIGNED", "Contract signed but email failed: " + emailError.getMessage());
                
                System.out.println("✅ Contract generated successfully (email failed)");
                
                // Devolver el PDF como bytes aunque el email falle
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", "contract-" + uid + "-signed.pdf");
                
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(pdfBytes);
            }
        } catch (Exception e) {
            System.err.println("❌ Error in generateAndSendContract: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
