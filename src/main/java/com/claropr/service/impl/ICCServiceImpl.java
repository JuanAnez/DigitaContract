package com.claropr.service.impl;

import com.claropr.model.IccLov;
import com.claropr.service.ICCService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ICCServiceImpl implements ICCService {

    private final JdbcTemplate jdbcTemplate;

    public ICCServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public IccLov getIccLovByKey(String key) {
        // For demo purposes, return a default LDAP configuration
        // In production, this should query the database
        if ("LDAP_AUTH_CTX_SOURCE".equals(key)) {
            return new IccLov(key, "ldap://10.0.144.162@389@CN=Service Account,OU=Service Accounts,DC=prt,DC=local@password123");
        }
        
        try {
            String sql = "SELECT LOV_KEY, LOV_DESCRIPTION FROM ICC_LOV WHERE LOV_KEY = ?";
            List<IccLov> lovs = jdbcTemplate.query(sql, new Object[]{key}, (rs, rowNum) -> {
                IccLov lov = new IccLov();
                lov.setLovKey(rs.getString("LOV_KEY"));
                lov.setLovDescription(rs.getString("LOV_DESCRIPTION"));
                return lov;
            });

            return lovs.isEmpty() ? new IccLov(key, "") : lovs.get(0);
        } catch (Exception e) {
            System.err.println("Error querying ICC_LOV table: " + e.getMessage());
            return new IccLov(key, "");
        }
    }

    @Override
    public void insertErrorInDB(String system, String summary, String details, String stackTrace, String className, String methodName) {
        try {
            String sql = "INSERT INTO ERROR_LOG (SYSTEM, SUMMARY, DETAILS, STACK_TRACE, CLASS_NAME, METHOD_NAME, CREATED_DATE) VALUES (?, ?, ?, ?, ?, ?, SYSDATE)";
            jdbcTemplate.update(sql, system, summary, details, stackTrace, className, methodName);
        } catch (Exception e) {
            // Log error but don't throw exception to avoid breaking the authentication flow
            System.err.println("Error inserting error log: " + e.getMessage());
            System.err.println("Error logged - System: " + system + ", Summary: " + summary);
        }
    }
}
