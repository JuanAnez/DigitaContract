package com.claropr.controller;

import com.claropr.model.ApiResponseDTO;
import com.claropr.model.LovItem;
import com.claropr.service.SirService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/sir")
public class SirController {

    private static final Logger logger = Logger.getLogger(SirController.class.getName());

    @Autowired
    private SirService sirService;

    @GetMapping("/oficinas-comerciales")
    public ResponseEntity<?> getOficinasComerciales() {
        try {
            logger.info("Iniciando consulta de oficinas comerciales desde SIR");
            List<LovItem> oficinas = sirService.getOficinasComerciales();
            logger.info("Consulta exitosa: " + oficinas.size() + " oficinas encontradas");
            
            ApiResponseDTO<List<LovItem>> response = ApiResponseDTO.create(
                200, "Success", oficinas
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.severe("Error al obtener oficinas comerciales: " + e.getMessage());
            e.printStackTrace();
            
            ApiResponseDTO<String> errorResponse = ApiResponseDTO.create(
                500, "Internal Server Error", "Error al obtener oficinas comerciales: " + e.getMessage()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/test-connection")
    public ResponseEntity<?> testConnection() {
        try {
            logger.info("Probando conexión a SIR database");
            List<LovItem> oficinas = sirService.getOficinasComerciales();
            logger.info("Conexión exitosa: " + oficinas.size() + " registros encontrados");
            
            ApiResponseDTO<String> response = ApiResponseDTO.create(
                200, "Success", "Conexión a SIR exitosa. " + oficinas.size() + " oficinas disponibles."
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.severe("Error de conexión a SIR: " + e.getMessage());
            e.printStackTrace();
            
            ApiResponseDTO<String> errorResponse = ApiResponseDTO.create(
                500, "Connection Error", "Error de conexión a SIR: " + e.getMessage()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
