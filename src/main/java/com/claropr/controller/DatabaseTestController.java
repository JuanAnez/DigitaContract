package com.claropr.controller;

import com.claropr.model.ApiResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class DatabaseTestController {

    @Autowired
    @Qualifier("icAdminJdbcTemplate")
    private JdbcTemplate icAdminJdbcTemplate;

    @GetMapping("/icadmin-connection")
    public ApiResponseDTO<Map<String, Object>> testIcAdminConnection() {
        try {
            // Probar conexión a IC_ADMIN
            String sql = "SELECT COUNT(*) as contract_count FROM IC_ADMIN.CONTRACTS";
            Integer count = icAdminJdbcTemplate.queryForObject(sql, Integer.class);
            
            Map<String, Object> result = new HashMap<>();
            result.put("contracts_count", count);
            result.put("database", "IC_ADMIN");
            
            return ApiResponseDTO.create(200, "Conexión a IC_ADMIN exitosa", result);
        } catch (Exception e) {
            return ApiResponseDTO.create(500, "Error conectando a IC_ADMIN: " + e.getMessage(), null);
        }
    }

    @GetMapping("/icadmin-contracts")
    public ApiResponseDTO<List<Map<String, Object>>> getIcAdminContracts() {
        try {
            String sql = "SELECT CONTRACT_UID, STATUS, CREATED_AT FROM IC_ADMIN.CONTRACTS ORDER BY CREATED_AT DESC";
            List<Map<String, Object>> contracts = icAdminJdbcTemplate.queryForList(sql);
            
            return ApiResponseDTO.create(200, "Contratos obtenidos exitosamente", contracts);
        } catch (Exception e) {
            return ApiResponseDTO.create(500, "Error obteniendo contratos: " + e.getMessage(), null);
        }
    }

    @GetMapping("/icadmin-audit")
    public ApiResponseDTO<List<Map<String, Object>>> getIcAdminAudit() {
        try {
            String sql = "SELECT c.CONTRACT_UID, a.ACTION, a.STATUS, a.MESSAGE, a.AT " +
                        "FROM IC_ADMIN.CONTRACTS c " +
                        "JOIN IC_ADMIN.CONTRACT_AUDIT_LOG a ON a.CONTRACT_ID = c.ID " +
                        "ORDER BY a.AT DESC";
            List<Map<String, Object>> audit = icAdminJdbcTemplate.queryForList(sql);
            
            return ApiResponseDTO.create(200, "Auditoría obtenida exitosamente", audit);
        } catch (Exception e) {
            return ApiResponseDTO.create(500, "Error obteniendo auditoría: " + e.getMessage(), null);
        }
    }
}
