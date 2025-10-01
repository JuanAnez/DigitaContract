package com.claropr.controller;

import com.claropr.model.ApiResponseDTO;
import com.claropr.model.SalesContractPayload;
import com.claropr.service.ContractEmailService;
import com.claropr.service.ContractPdfService;
import com.claropr.service.PrefillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private static final Logger logger = Logger.getLogger(ContractController.class.getName());

    private final PrefillService prefillService;
    private final ContractPdfService contractPdfService;
    private final ContractEmailService contractEmailService;

    @Autowired
    public ContractController(PrefillService prefillService, 
                             ContractPdfService contractPdfService,
                             ContractEmailService contractEmailService) {
        this.prefillService = prefillService;
        this.contractPdfService = contractPdfService;
        this.contractEmailService = contractEmailService;
    }

    @GetMapping("/{uid}/prefill")
    public ResponseEntity<?> getContractPrefill(@PathVariable("uid") String uid) {
        try {
            logger.info("Getting contract prefill for UID: " + uid);
            
            // Obtener datos del contrato
            SalesContractPayload contractData = prefillService.getPrefill(uid);
            
            ApiResponseDTO<SalesContractPayload> response = ApiResponseDTO.create(
                    HttpStatus.OK.value(), 
                    "Datos del contrato obtenidos exitosamente", 
                    contractData
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            logger.severe("Error getting contract prefill for " + uid + ": " + e.getMessage());
            e.printStackTrace();
            
            ApiResponseDTO<String> errorResponse = ApiResponseDTO.create(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Error obteniendo datos del contrato", 
                    "Error al obtener los datos del contrato: " + e.getMessage()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
