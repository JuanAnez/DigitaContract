package com.claropr.dao.impl;

import com.claropr.dao.AccessTokenDao;
import com.claropr.model.AccessTokenData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class AccessTokenDaoImpl implements AccessTokenDao {

    private final JdbcTemplate jdbcTemplate;

    public AccessTokenDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AccessTokenData getAccessTokenByToken(String token) {
        String sql = "SELECT * FROM IC_ADMIN.APP_ACCESS_TOKEN WHERE ACCESS_TOKEN = ?";
        
        List<AccessTokenData> tokens = jdbcTemplate.query(sql, new Object[]{token}, (rs, rowNum) -> {
            AccessTokenData accessToken = new AccessTokenData();
            accessToken.setToken(rs.getString("ACCESS_TOKEN"));
            accessToken.setUsername(rs.getString("USER_ACCESS"));
            accessToken.setCreatedDate(rs.getTimestamp("CREATION_DATE"));
            
            // Calcular fecha de expiración (CREATION_DATE + 10 minutos)
            Date createdDate = rs.getTimestamp("CREATION_DATE");
            if (createdDate != null) {
                Date expirationDate = new Date(createdDate.getTime() + (10 * 60 * 1000)); // 10 minutos
                accessToken.setExpirationDate(expirationDate);
            }
            
            accessToken.setValid(rs.getTimestamp("REVOKED_AT") == null);
            return accessToken;
        });

        return tokens.isEmpty() ? new AccessTokenData() : tokens.get(0);
    }

    @Override
    public void saveAccessToken(String token, String username) {
        String sql = "INSERT INTO IC_ADMIN.APP_ACCESS_TOKEN (ID, ACCESS_TOKEN, USER_ACCESS, REVOKED_AT, "
            + "CREATION_DATE, CREATION_USER) "
            + "VALUES (IC_ADMIN.APP_ACCESS_TOKEN_SEQ.NEXTVAL, ?, ?, ?, ?, ?)";
        
        Date now = new Date();
        
        jdbcTemplate.update(sql, token, username, null, now, "SYSTEM");
    }

    @Override
    public void revokeAccessToken(String token) {
        String sql = "UPDATE IC_ADMIN.APP_ACCESS_TOKEN SET REVOKED_AT = ?, UPDATE_DATE = ?,"
            + "UPDATE_USER = ? WHERE ACCESS_TOKEN = ?";
        Date now = new Date();
        jdbcTemplate.update(sql, now, now, "SYSTEM", token);
    }
}
