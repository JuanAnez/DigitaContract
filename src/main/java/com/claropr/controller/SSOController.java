package com.claropr.controller;

import com.claropr.dao.AccessTokenDao;
import com.claropr.model.ApiResponseDTO;
import com.claropr.model.AzureADToken;
import com.claropr.model.AzureADUser;
import com.claropr.model.LoginResponse;
import com.claropr.security.JwtUtil;
import com.claropr.service.MockAzureADService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/auth/azure")
public class SSOController {
    
    @Autowired
    private MockAzureADService azureADService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AccessTokenDao accessTokenDao;
    
    @GetMapping("/login")
    public void azureLogin(HttpServletResponse response) throws IOException {
        String authUrl = azureADService.buildAuthUrl();
        System.out.println("SSOController: Redirecting to Azure AD: " + authUrl);
        response.sendRedirect(authUrl);
    }
    
    @GetMapping("/callback")
    public ResponseEntity<?> azureCallback(@RequestParam String code, @RequestParam(required = false) String state) {
        try {
            System.out.println("SSOController: Azure AD callback received with code: " + code);
            
            // 1. Intercambiar código por token
            AzureADToken token = azureADService.exchangeCodeForToken(code);
            System.out.println("SSOController: Token received: " + token.getAccessToken());
            
            // 2. Obtener información del usuario
            AzureADUser azureUser = azureADService.getUserInfo(token);
            System.out.println("SSOController: User info received: " + azureUser.getMail());
            
            // 3. Validar que el usuario existe en LDAP (simulado)
            if (!isUserValidInLDAP(azureUser)) {
                System.out.println("SSOController: User not found in LDAP: " + azureUser.getMail());
                ApiResponseDTO<String> dto = ApiResponseDTO.create(
                    401, "Unauthorized", "User not found in LDAP: " + azureUser.getMail()
                );
                return new ResponseEntity<>(dto, HttpStatus.UNAUTHORIZED);
            }
            
            // 4. Crear JWT interno
            String jwt = jwtUtil.generateToken(azureUser.getMail());
            System.out.println("SSOController: JWT created: " + jwt);
            
            // 5. Guardar token en base de datos
            accessTokenDao.saveAccessToken(jwt, azureUser.getMail());
            System.out.println("SSOController: Token saved to database");
            
            // 6. Retornar respuesta exitosa
            LoginResponse loginResponse = new LoginResponse(
                jwt, 
                azureUser.getMail(),
                "Authentication successful via Azure AD"
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
            
            System.out.println("SSOController: Returning successful response");
            return new ResponseEntity<>(loginResponse, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            System.out.println("SSOController: Azure AD callback error: " + e.getMessage());
            e.printStackTrace();
            
            ApiResponseDTO<String> dto = ApiResponseDTO.create(
                500, "Internal Server Error", "Authentication failed: " + e.getMessage()
            );
            return new ResponseEntity<>(dto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    private boolean isUserValidInLDAP(AzureADUser azureUser) {
        // Simular validación en LDAP
        // En implementación real, aquí buscarías el usuario en LDAP
        String email = azureUser.getMail();
        
        System.out.println("SSOController: Validating user in LDAP: " + email);
        
        // Simular usuarios válidos
        boolean isValid = email != null && (
            email.equals("mock.user@company.com") ||
            email.equals("admin@company.com") ||
            email.equals("user@company.com")
        );
        
        System.out.println("SSOController: User validation result: " + isValid);
        return isValid;
    }
}
