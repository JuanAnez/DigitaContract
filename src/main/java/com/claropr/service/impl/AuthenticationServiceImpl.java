package com.claropr.service.impl;

import com.claropr.dao.AccessTokenDao;
import com.claropr.dao.IccUserDao;
import com.claropr.exceptions.BusinessLogicException;
import com.claropr.model.AccessTokenData;
import com.claropr.model.LoginRequestData;
import com.claropr.security.JwtUtil;
import com.claropr.service.AuthenticationService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final AccessTokenDao accessTokenDao;
  private final IccUserDao iccUserDao;

  public AuthenticationServiceImpl(
      AuthenticationManager authenticationManager,
      JwtUtil jwtUtil,
      AccessTokenDao accessTokenDao,
      IccUserDao iccUserDao) {
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
    this.accessTokenDao = accessTokenDao;
    this.iccUserDao = iccUserDao;
  }

  @Override
  public String authenticate(LoginRequestData loginRequestData) throws Exception {
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  loginRequestData.getUsername(), loginRequestData.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);

      Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

      StringBuilder roles = new StringBuilder();

      for (GrantedAuthority authority : authorities) {
        roles.append(authority.getAuthority()).append(", ");
      }

      String accessToken = jwtUtil.generateToken(loginRequestData.getUsername());

      this.saveAccessToken(accessToken, loginRequestData.getUsername());

      return accessToken;
    } catch (BadCredentialsException e) {
      throw new BusinessLogicException(401, "Incorrect username or password");
    } catch (UsernameNotFoundException e) {
      throw new BusinessLogicException(404, "The user does not exist");
    } catch (io.jsonwebtoken.SignatureException e) {
      throw new BusinessLogicException(400, "Invalid JWT signature");
    } catch (io.jsonwebtoken.ExpiredJwtException e) {
      throw new BusinessLogicException(401, "JWT token has expired");
    }
  }

  @Override
  public void logout(String accessToken) {
    // Validate the token before revoking
    if (accessToken == null || accessToken.trim().isEmpty()) {
      throw new BusinessLogicException(400, "Access token is required");
    }
    
    // Log the token for debugging (first 20 chars only for security)
    System.out.println("Logout attempt with token: " + accessToken.substring(0, Math.min(20, accessToken.length())) + "...");
    
    // Extract username from token and validate JWT format
    String username;
    try {
      username = jwtUtil.extractUsername(accessToken);
    } catch (Exception e) {
      // Log the error for debugging
      System.err.println("JWT extraction error: " + e.getMessage());
      throw new BusinessLogicException(401, "Invalid access token format");
    }
    
    // Check if token is valid JWT format
    if (!jwtUtil.validateToken(accessToken, username)) {
      throw new BusinessLogicException(401, "Invalid access token");
    }
    
    // Check if token exists in database
    AccessTokenData tokenData = accessTokenDao.getAccessTokenByToken(accessToken);
    if (tokenData == null || tokenData.getToken() == null) {
      throw new BusinessLogicException(404, "Access token not found");
    }
    
    // Check if token is already revoked
    if (!tokenData.isValid()) {
      throw new BusinessLogicException(401, "Access token is already revoked");
    }
    
    // Revoke the token
    accessTokenDao.revokeAccessToken(accessToken);
  }

  private void saveAccessToken(String accessToken, String username) {
    accessTokenDao.saveAccessToken(accessToken, username);
  }
}
