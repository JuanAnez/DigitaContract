package com.claropr.controller;

import com.claropr.dao.AccessTokenDao;
import com.claropr.model.AccessTokenData;
import com.claropr.model.ApiResponseDTO;
import com.claropr.model.LoginRequestData;
import com.claropr.model.LogoutRequestData;
import com.claropr.security.JwtUtil;
import com.claropr.service.AuthenticationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.owasp.encoder.Encode;

@RestController
@RequestMapping("/api")
public class LoginController {
    private final AuthenticationService authenticationService;
    private final JwtUtil jwtUtil;
    private final AccessTokenDao accessTokenDao;

    public LoginController(AuthenticationService authenticationService, JwtUtil jwtUtil, AccessTokenDao accessTokenDao) {
        this.authenticationService = authenticationService;
        this.jwtUtil = jwtUtil;
        this.accessTokenDao = accessTokenDao;
    }

    @PostMapping(path="/login-ws",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> login(@RequestBody LoginRequestData loginRequestData) {
        HttpHeaders headers = getHttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            String accessToken = authenticationService.authenticate(loginRequestData);
            String safe = Encode.forHtmlContent(accessToken);

            ApiResponseDTO<String> dto = ApiResponseDTO.create(HttpStatus.OK.value(), "OK", safe);
            return new ResponseEntity<>(dto, headers, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponseDTO<String> dto = ApiResponseDTO.create(
                    HttpStatus.UNAUTHORIZED.value(),
                    "AUTHENTICATION_FAILED",
                    "Authentication failed: " + e.getMessage()
            );
            return new ResponseEntity<>(dto, headers, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping(
            path = "/logout-ws",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> logout(@RequestBody LogoutRequestData logoutRequestData) {
        HttpHeaders headers = getHttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            // Validate token format first
            if (logoutRequestData.getAccessToken() == null || logoutRequestData.getAccessToken().trim().isEmpty()) {
                ApiResponseDTO<String> dto = ApiResponseDTO.create(400, "Bad Request", "Access token is required");
                return new ResponseEntity<>(dto, headers, HttpStatus.BAD_REQUEST);
            }
            
            // Try to extract username to validate JWT format
            try {
                String username = jwtUtil.extractUsername(logoutRequestData.getAccessToken());
                System.out.println("Logout: Extracted username: " + username);
            } catch (Exception e) {
                System.out.println("Logout: JWT extraction error: " + e.getMessage());
                ApiResponseDTO<String> dto = ApiResponseDTO.create(401, "Unauthorized", "Invalid access token format");
                return new ResponseEntity<>(dto, headers, HttpStatus.UNAUTHORIZED);
            }
            
            // Check if token exists in database
            AccessTokenData tokenData = accessTokenDao.getAccessTokenByToken(logoutRequestData.getAccessToken());
            if (tokenData == null || tokenData.getToken() == null) {
                ApiResponseDTO<String> dto = ApiResponseDTO.create(404, "Not Found", "Access token not found");
                return new ResponseEntity<>(dto, headers, HttpStatus.NOT_FOUND);
            }
            
            // Check if token is already revoked
            if (!tokenData.isValid()) {
                ApiResponseDTO<String> dto = ApiResponseDTO.create(401, "Unauthorized", "Access token is already revoked");
                return new ResponseEntity<>(dto, headers, HttpStatus.UNAUTHORIZED);
            }
            
            // Revoke the token
            accessTokenDao.revokeAccessToken(logoutRequestData.getAccessToken());
            
            ApiResponseDTO<String> dto = ApiResponseDTO.create(HttpStatus.OK.value(), "OK", "Logout successful");
            return new ResponseEntity<>(dto, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            System.out.println("Logout: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            ApiResponseDTO<String> dto = ApiResponseDTO.create(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "LOGOUT_FAILED",
                    "Logout failed: " + e.getMessage()
            );
            return new ResponseEntity<>(dto, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        return headers;
    }
}
