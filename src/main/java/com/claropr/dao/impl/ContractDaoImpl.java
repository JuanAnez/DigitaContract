package com.claropr.dao.impl;

import com.claropr.dao.ContractDao;
import com.claropr.model.ContractRecord;
import com.claropr.model.ContractParty;
import com.claropr.model.DeliveryEvent;
import com.claropr.model.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class ContractDaoImpl implements ContractDao {

    @Autowired
    @Qualifier("icAdminJdbcTemplate")
    private JdbcTemplate icAdminJdbcTemplate;

    @Override
    public void createContract(ContractRecord contract) {
        try {
            System.out.println("=== ContractDaoImpl.createContract START ===");
            System.out.println("Contract UID: " + contract.getContractUid());
            System.out.println("Contract Type: " + contract.getContractType());
            System.out.println("Status: " + contract.getStatus());
            
            String sql = "INSERT INTO IC_ADMIN.CONTRACTS (" +
                "CONTRACT_UID, CONTRACT_TYPE, ACCOUNT_TYPE, BAN_NUMBER, SUBSCRIBER_NUMBER, " +
                "SOURCE_SYSTEM, EXTERNAL_ID, TEMPLATE_ID, VERSION, STATUS, STATUS_MESSAGE, " +
                "FILE_URI, FILE_SHA256, SIGNED_PDF_PATH, PDF_GENERATED_AT, CREATED_BY_USER_ID, CREATED_AT, UPDATED_AT" +
                ") VALUES (" +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP" +
                ")";
            
            System.out.println("SQL: " + sql);
            
            // Crear KeyHolder para obtener el ID generado
            org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
            
            int rowsAffected = icAdminJdbcTemplate.update(
                new org.springframework.jdbc.core.PreparedStatementCreator() {
                    @Override
                    public java.sql.PreparedStatement createPreparedStatement(java.sql.Connection connection) throws java.sql.SQLException {
                        java.sql.PreparedStatement ps = connection.prepareStatement(sql, new String[]{"ID"});
                        ps.setString(1, contract.getContractUid());
                        ps.setString(2, contract.getContractType());
                        ps.setString(3, contract.getAccountType());
                        ps.setString(4, contract.getBanNumber());
                        ps.setString(5, contract.getSubscriberNumber());
                        ps.setString(6, contract.getSourceSystem());
                        ps.setString(7, contract.getExternalId());
                        ps.setString(8, contract.getTemplateId());
                        ps.setInt(9, contract.getVersion());
                        ps.setString(10, contract.getStatus());
                        ps.setString(11, contract.getStatusMessage());
                        ps.setString(12, contract.getFileUri());
                        ps.setString(13, contract.getFileSha256());
                        ps.setString(14, contract.getSignedPdfPath());
                        ps.setTimestamp(15, contract.getPdfGeneratedAt());
                        ps.setString(16, contract.getCreatedByUserId());
                        return ps;
                    }
                },
                keyHolder
            );
            
            // Obtener el ID generado y asignarlo al contrato
            if (keyHolder.getKey() != null) {
                String generatedId = keyHolder.getKey().toString();
                contract.setId(generatedId);
                System.out.println("Generated ID: " + generatedId);
            }
            
            System.out.println("Rows affected: " + rowsAffected);
            System.out.println("=== ContractDaoImpl.createContract SUCCESS ===");
            
        } catch (Exception e) {
            System.err.println("=== ContractDaoImpl.createContract ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void updateContractStatus(String contractUid, String status, String statusMessage) {
        String sql = "UPDATE IC_ADMIN.CONTRACTS SET STATUS = ?, STATUS_MESSAGE = ?, UPDATED_AT = SYSTIMESTAMP WHERE CONTRACT_UID = ?";
        icAdminJdbcTemplate.update(sql, status, statusMessage, contractUid);
    }

    @Override
    public void updateSignedPdf(String contractUid, String signedPdfPath, String sha256) {
        String sql = "UPDATE IC_ADMIN.CONTRACTS SET SIGNED_PDF_PATH = ?, SIGNED_PDF_SHA256 = ?, PDF_GENERATED_AT = SYSTIMESTAMP, UPDATED_AT = SYSTIMESTAMP WHERE CONTRACT_UID = ?";
        icAdminJdbcTemplate.update(sql, signedPdfPath, sha256, contractUid);
    }

    @Override
    public void updateSignedPdfWithObjectStorage(String contractUid, String objectUri, String sha256, String storageType) {
        String sql = "UPDATE IC_ADMIN.CONTRACTS SET SIGNED_PDF_URI = ?, SIGNED_PDF_SHA256 = ?, PDF_STORAGE_TYPE = ?, PDF_GENERATED_AT = SYSTIMESTAMP, UPDATED_AT = SYSTIMESTAMP WHERE CONTRACT_UID = ?";
        icAdminJdbcTemplate.update(sql, objectUri, sha256, storageType, contractUid);
    }

    @Override
    public void markSigned(String contractUid, String signedPdfUri, String signedPdfSha256, 
                         Timestamp customerSignedAt, Timestamp agentSignedAt) {
        String sql = "UPDATE IC_ADMIN.CONTRACTS SET " +
            "SIGNED_PDF_URI = ?, SIGNED_PDF_SHA256 = ?, " +
            "CUSTOMER_SIGNED_AT = ?, AGENT_SIGNED_AT = ?, " +
            "STATUS = 'SIGNED', UPDATED_AT = SYSTIMESTAMP " +
            "WHERE CONTRACT_UID = ?";
        icAdminJdbcTemplate.update(sql, signedPdfUri, signedPdfSha256, customerSignedAt, agentSignedAt, contractUid);
    }

    @Override
    public ContractRecord getContractByUid(String contractUid) {
        String sql = "SELECT * FROM IC_ADMIN.CONTRACTS WHERE CONTRACT_UID = ?";
        return icAdminJdbcTemplate.queryForObject(sql, new ContractRecordRowMapper(), contractUid);
    }

    @Override
    public List<ContractRecord> getContractsByStatus(String status) {
        String sql = "SELECT * FROM IC_ADMIN.CONTRACTS WHERE STATUS = ? ORDER BY CREATED_AT DESC";
        return icAdminJdbcTemplate.query(sql, new ContractRecordRowMapper(), status);
    }

    @Override
    public List<ContractRecord> getAllContracts() {
        String sql = "SELECT * FROM IC_ADMIN.CONTRACTS ORDER BY CREATED_AT DESC";
        return icAdminJdbcTemplate.query(sql, new ContractRecordRowMapper());
    }

    @Override
    public void addContractParty(ContractParty party) {
        try {
            System.out.println("=== ContractDaoImpl.addContractParty START ===");
            System.out.println("Contract ID: " + party.getContractId());
            System.out.println("Role: " + party.getRole());
            System.out.println("Full Name: " + party.getFullName());
            
            String sql = "INSERT INTO IC_ADMIN.CONTRACT_PARTIES (" +
                "CONTRACT_ID, ROLE, FULL_NAME, EMAIL, PHONE, CREATED_AT" +
                ") VALUES (" +
                "?, ?, ?, ?, ?, SYSTIMESTAMP" +
                ")";
            
            int rowsAffected = icAdminJdbcTemplate.update(sql, party.getContractId(), party.getRole(),
                party.getFullName(), party.getEmail(), party.getPhone());
            
            System.out.println("Rows affected: " + rowsAffected);
            System.out.println("=== ContractDaoImpl.addContractParty SUCCESS ===");
            
        } catch (Exception e) {
            System.err.println("=== ContractDaoImpl.addContractParty ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<ContractParty> getContractParties(String contractId) {
        String sql = "SELECT * FROM IC_ADMIN.CONTRACT_PARTIES WHERE CONTRACT_ID = ?";
        return icAdminJdbcTemplate.query(sql, new ContractPartyRowMapper(), contractId);
    }

    @Override
    public void logAudit(String contractUid, String action, String status, String message, String actor) {
        try {
            System.out.println("=== ContractDaoImpl.logAudit START ===");
            System.out.println("Contract UID: " + contractUid);
            System.out.println("Action: " + action);
            System.out.println("Status: " + status);
            System.out.println("Actor: " + actor);
            
            // Primero obtener el ID del contrato
            String getContractIdSql = "SELECT ID FROM IC_ADMIN.CONTRACTS WHERE CONTRACT_UID = ?";
            String contractId = icAdminJdbcTemplate.queryForObject(getContractIdSql, String.class, contractUid);
            
            System.out.println("Found Contract ID: " + contractId);
            
            // Luego insertar el log de auditoría usando el ID directo
            String sql = "INSERT INTO IC_ADMIN.CONTRACT_AUDIT_LOG (" +
                "CONTRACT_ID, ACTOR_USER_ID, ACTION, STATUS, MESSAGE, AT" +
                ") VALUES (" +
                "?, ?, ?, ?, ?, SYSTIMESTAMP" +
                ")";
            
            int rowsAffected = icAdminJdbcTemplate.update(sql, contractId, actor, action, status, message);
            
            System.out.println("Rows affected: " + rowsAffected);
            System.out.println("=== ContractDaoImpl.logAudit SUCCESS ===");
            
        } catch (Exception e) {
            System.err.println("=== ContractDaoImpl.logAudit ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<AuditLog> getAuditLog(String contractId) {
        String sql = "SELECT * FROM IC_ADMIN.CONTRACT_AUDIT_LOG WHERE CONTRACT_ID = ? ORDER BY AT DESC";
        return icAdminJdbcTemplate.query(sql, new AuditLogRowMapper(), contractId);
    }

    @Override
    public void logDelivery(String contractUid, String channel, String address, String status,
                           String providerMsgId, String errorMessage) {
        try {
            System.out.println("=== ContractDaoImpl.logDelivery START ===");
            System.out.println("Contract UID: " + contractUid);
            System.out.println("Channel: " + channel);
            System.out.println("Address: " + address);
            System.out.println("Status: " + status);
            
            // Primero obtener el ID del contrato
            String getContractIdSql = "SELECT ID FROM IC_ADMIN.CONTRACTS WHERE CONTRACT_UID = ?";
            String contractId = icAdminJdbcTemplate.queryForObject(getContractIdSql, String.class, contractUid);
            
            System.out.println("Found Contract ID: " + contractId);
            
            // Luego insertar el evento de entrega usando el ID directo
            String sql = "INSERT INTO IC_ADMIN.DELIVERY_EVENTS (" +
                "CONTRACT_ID, CHANNEL, ADDRESS, STATUS, PROVIDER_MSG_ID, ERROR_MESSAGE, SENT_AT" +
                ") VALUES (" +
                "?, ?, ?, ?, ?, ?, SYSTIMESTAMP" +
                ")";
            
            int rowsAffected = icAdminJdbcTemplate.update(sql, contractId, channel, address, status, providerMsgId, errorMessage);
            
            System.out.println("Rows affected: " + rowsAffected);
            System.out.println("=== ContractDaoImpl.logDelivery SUCCESS ===");
            
        } catch (Exception e) {
            System.err.println("=== ContractDaoImpl.logDelivery ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<DeliveryEvent> getDeliveryEvents(String contractId) {
        String sql = "SELECT * FROM IC_ADMIN.DELIVERY_EVENTS WHERE CONTRACT_ID = ? ORDER BY SENT_AT DESC";
        return icAdminJdbcTemplate.query(sql, new DeliveryEventRowMapper(), contractId);
    }

    // Row Mappers
    private static class ContractRecordRowMapper implements RowMapper<ContractRecord> {
        @Override
        public ContractRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            ContractRecord record = new ContractRecord();
            record.setId(rs.getString("ID"));
            record.setContractUid(rs.getString("CONTRACT_UID"));
            record.setContractType(rs.getString("CONTRACT_TYPE"));
            record.setAccountType(rs.getString("ACCOUNT_TYPE"));
            record.setBanNumber(rs.getString("BAN_NUMBER"));
            record.setSubscriberNumber(rs.getString("SUBSCRIBER_NUMBER"));
            record.setSourceSystem(rs.getString("SOURCE_SYSTEM"));
            record.setExternalId(rs.getString("EXTERNAL_ID"));
            record.setTemplateId(rs.getString("TEMPLATE_ID"));
            record.setVersion(rs.getInt("VERSION"));
            record.setStatus(rs.getString("STATUS"));
            record.setStatusMessage(rs.getString("STATUS_MESSAGE"));
            record.setFileUri(rs.getString("FILE_URI"));
            record.setFileSha256(rs.getString("FILE_SHA256"));
                   record.setSignedPdfUri(rs.getString("SIGNED_PDF_URI"));
                   record.setSignedPdfSha256(rs.getString("SIGNED_PDF_SHA256"));
                   record.setSignedPdfPath(rs.getString("SIGNED_PDF_PATH"));
                   record.setPdfGeneratedAt(rs.getTimestamp("PDF_GENERATED_AT"));
                   record.setCustomerSignedAt(rs.getTimestamp("CUSTOMER_SIGNED_AT"));
                   record.setAgentSignedAt(rs.getTimestamp("AGENT_SIGNED_AT"));
                   record.setCreatedByUserId(rs.getString("CREATED_BY_USER_ID"));
                   record.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                   record.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
            return record;
        }
    }

    private static class ContractPartyRowMapper implements RowMapper<ContractParty> {
        @Override
        public ContractParty mapRow(ResultSet rs, int rowNum) throws SQLException {
            ContractParty party = new ContractParty();
            party.setId(rs.getString("ID"));
            party.setContractId(rs.getString("CONTRACT_ID"));
            party.setRole(rs.getString("ROLE"));
            party.setFullName(rs.getString("FULL_NAME"));
            party.setEmail(rs.getString("EMAIL"));
            party.setPhone(rs.getString("PHONE"));
            party.setCreatedAt(rs.getTimestamp("CREATED_AT"));
            return party;
        }
    }

    private static class AuditLogRowMapper implements RowMapper<AuditLog> {
        @Override
        public AuditLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            AuditLog log = new AuditLog();
            log.setId(rs.getString("ID"));
            log.setContractId(rs.getString("CONTRACT_ID"));
            log.setActorUserId(rs.getString("ACTOR_USER_ID"));
            log.setAction(rs.getString("ACTION"));
            log.setStatus(rs.getString("STATUS"));
            log.setMessage(rs.getString("MESSAGE"));
            log.setAt(rs.getTimestamp("AT"));
            return log;
        }
    }

    private static class DeliveryEventRowMapper implements RowMapper<DeliveryEvent> {
        @Override
        public DeliveryEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            DeliveryEvent event = new DeliveryEvent();
            event.setId(rs.getString("ID"));
            event.setContractId(rs.getString("CONTRACT_ID"));
            event.setChannel(rs.getString("CHANNEL"));
            event.setAddress(rs.getString("ADDRESS"));
            event.setStatus(rs.getString("STATUS"));
            event.setProviderMsgId(rs.getString("PROVIDER_MSG_ID"));
            event.setErrorMessage(rs.getString("ERROR_MESSAGE"));
            event.setSentAt(rs.getTimestamp("SENT_AT"));
            return event;
        }
    }
}
