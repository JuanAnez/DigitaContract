package com.claropr.controller;

import com.claropr.model.ApiResponseDTO;
import com.claropr.model.SalesContractPayload;
import com.claropr.service.PrefillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchController {

    @Autowired
    private PrefillService prefillService;

    /**
     * Búsqueda por tipo: Venta nueva desde SIF
     * Caso de uso #8
     */
    @GetMapping("/sif/{contractUid}")
    public ApiResponseDTO<SalesContractPayload> searchNewSaleFromSIF(@PathVariable String contractUid) {
        try {
            System.out.println("=== SearchController.searchNewSaleFromSIF ===");
            System.out.println("Contract UID: " + contractUid);
            
            SalesContractPayload payload = prefillService.searchNewSaleFromSIF(contractUid);
            
            return ApiResponseDTO.create(200, "Búsqueda exitosa desde SIF", payload);
        } catch (Exception e) {
            System.err.println("Error en búsqueda SIF: " + e.getMessage());
            return ApiResponseDTO.create(500, "Error en búsqueda desde SIF: " + e.getMessage(), null);
        }
    }

    /**
     * Búsqueda por tipo: Venta nueva desde COPS
     * Caso de uso #9
     */
    @GetMapping("/cops/{contractUid}")
    public ApiResponseDTO<SalesContractPayload> searchNewSaleFromCOPS(
            @PathVariable String contractUid,
            @RequestParam(required = false) String orderNumber) {
        try {
            System.out.println("=== SearchController.searchNewSaleFromCOPS ===");
            System.out.println("Contract UID: " + contractUid);
            System.out.println("Order Number: " + orderNumber);
            
            SalesContractPayload payload = prefillService.searchNewSaleFromCOPS(contractUid, orderNumber);
            
            return ApiResponseDTO.create(200, "Búsqueda exitosa desde COPS", payload);
        } catch (Exception e) {
            System.err.println("Error en búsqueda COPS: " + e.getMessage());
            return ApiResponseDTO.create(500, "Error en búsqueda desde COPS: " + e.getMessage(), null);
        }
    }

    /**
     * Búsqueda por tipo: Historial
     * Caso de uso #6
     */
    @GetMapping("/history/{contractUid}")
    public ApiResponseDTO<SalesContractPayload> searchHistory(@PathVariable String contractUid) {
        try {
            System.out.println("=== SearchController.searchHistory ===");
            System.out.println("Contract UID: " + contractUid);
            
            SalesContractPayload payload = prefillService.searchHistory(contractUid);
            
            return ApiResponseDTO.create(200, "Búsqueda de historial exitosa", payload);
        } catch (Exception e) {
            System.err.println("Error en búsqueda de historial: " + e.getMessage());
            return ApiResponseDTO.create(500, "Error en búsqueda de historial: " + e.getMessage(), null);
        }
    }

    /**
     * Búsqueda unificada con tipo de búsqueda
     * Caso de uso #4, #5, #6, #7
     */
    @PostMapping("/unified")
    public ApiResponseDTO<SalesContractPayload> unifiedSearch(@RequestBody Map<String, String> searchParams) {
        try {
            String contractUid = searchParams.get("contractUid");
            String searchType = searchParams.get("searchType");
            String orderNumber = searchParams.get("orderNumber");
            
            System.out.println("=== SearchController.unifiedSearch ===");
            System.out.println("Contract UID: " + contractUid);
            System.out.println("Search Type: " + searchType);
            System.out.println("Order Number: " + orderNumber);
            
            SalesContractPayload payload = prefillService.getPrefill(contractUid);
            
            return ApiResponseDTO.create(200, "Búsqueda unificada exitosa", payload);
        } catch (Exception e) {
            System.err.println("Error en búsqueda unificada: " + e.getMessage());
            return ApiResponseDTO.create(500, "Error en búsqueda unificada: " + e.getMessage(), null);
        }
    }
}
