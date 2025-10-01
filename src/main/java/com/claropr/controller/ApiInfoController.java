package com.claropr.controller;

import com.claropr.model.ApiResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiInfoController {

    @GetMapping("/info")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getApiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "Contract Authentication Service");
        info.put("version", "1.0.0");
        info.put("company", "ClaroPR");
        info.put("endpoints", new String[]{
            "POST /contract/api/login-ws",
            "POST /contract/api/logout-ws"
        });
        info.put("testUsers", new String[]{
            "train_ic_user / claro123",
            "train_ic_admin / claro123", 
            "isy94545 / claro123"
        });

        ApiResponseDTO<Map<String, Object>> response = ApiResponseDTO.create(
            HttpStatus.OK.value(), 
            "OK", 
            info
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

