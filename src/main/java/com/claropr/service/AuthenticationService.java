package com.claropr.service;

import com.claropr.model.LoginRequestData;

public interface AuthenticationService {
    String authenticate(LoginRequestData loginRequestData) throws Exception;
    void logout(String accessToken);
}

