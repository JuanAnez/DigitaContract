package com.claropr.dao;

import com.claropr.model.AccessTokenData;

public interface AccessTokenDao {
    AccessTokenData getAccessTokenByToken(String token);
    void saveAccessToken(String token, String username);
    void revokeAccessToken(String token);
}

