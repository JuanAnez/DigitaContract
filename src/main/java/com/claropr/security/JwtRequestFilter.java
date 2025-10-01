package com.claropr.security;

import com.claropr.dao.AccessTokenDao;
import com.claropr.model.AccessTokenData;
import com.claropr.model.ApiResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
  private final JwtUtil jwtUtil;
  private final AccessTokenDao accessTokenDao;

  private final ObjectMapper objectMapper = new ObjectMapper();

  public JwtRequestFilter(JwtUtil jwtUtil, AccessTokenDao accessTokenDao) {
    this.jwtUtil = jwtUtil;
    this.accessTokenDao = accessTokenDao;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
          throws ServletException, IOException {

    String rid = (String) request.getAttribute("X-Request-Id");
    if (rid == null || rid.isEmpty()) {
      rid = java.util.UUID.randomUUID().toString();
      request.setAttribute("X-Request-Id", rid);
    }

    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      chain.doFilter(request, response);
      return;
    }

    String uri = request.getRequestURI();

    if (!uri.startsWith("/contract/api/")) {
      chain.doFilter(request, response);
      return;
    }

    boolean isPublicApi =
            uri.equals("/contract/api/login-ws") ||
                    uri.equals("/contract/api/logout-ws") ||
                    uri.equals("/contract/api/auth/azure/login") ||
                    uri.equals("/contract/api/auth/azure/callback");

    if (isPublicApi) {
      chain.doFilter(request, response);
      return;
    }

    String jwt = this.extractJwtFromRequest(request, response);
    if (jwt == null) return;

    String username = this.extractUsernameFromJwt(jwt, response);
    if (username == null) return;

    if (this.isTokenValid(jwt, username, response)) {
      this.grantAccess(jwt, username);
      chain.doFilter(request, response);
    }
    // Si el token no es válido, isTokenValid ya envió la respuesta de error
  }

  private String extractJwtFromRequest(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String authorizationHeader = request.getHeader("Authorization");

    if (authorizationHeader == null) {
      this.sendErrorResponse(response, "To consume this service you must send the authorization");

      return null;
    }

    if (!authorizationHeader.startsWith("Bearer ")) {
      this.sendErrorResponse(response, "Incorrect authentication type, must be Bearer");

      return null;
    }

    return authorizationHeader.substring(7);
  }

  private String extractUsernameFromJwt(String jwt, HttpServletResponse response)
      throws IOException {
    try {
      return jwtUtil.extractUsername(jwt);
    } catch (Exception e) {
      this.sendErrorResponse(response, "Invalid token format");

      return null;
    }
  }

  private boolean isTokenValid(String jwt, String username, HttpServletResponse response)
      throws IOException {
    try {
      if (!jwtUtil.validateToken(jwt, username)) {
        this.sendErrorResponse(response, "Token is not valid or has expired");

        return false;
      }

      AccessTokenData accessTokenData = accessTokenDao.getAccessTokenByToken(jwt);

      if (!accessTokenData.isValidToken()) {
        this.sendErrorResponse(response, "Token is not valid or has been revoked");

        return false;
      }

      return true;
    } catch (Exception e) {
      this.sendErrorResponse(response, "Unauthorized access, invalid token");

      return false;
    }
  }

  private void grantAccess(String jwt, String username) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>()));
  }

  private void sendErrorResponse(HttpServletResponse response, String messageError)
      throws IOException {
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");
    response.setHeader("X-Content-Type-Options", "nosniff");

    ApiResponseDTO<String> apiResponseDTO =
        ApiResponseDTO.create(
            HttpStatus.UNAUTHORIZED.value(),
            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            messageError);

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    // Use getOutputStream() instead of getWriter() to avoid conflicts
    response.getOutputStream().write(objectMapper.writeValueAsBytes(apiResponseDTO));
  }
}
