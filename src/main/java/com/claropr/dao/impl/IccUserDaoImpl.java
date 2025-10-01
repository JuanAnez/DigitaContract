package com.claropr.dao.impl;

import com.claropr.dao.IccUserDao;
import com.claropr.model.Users;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class IccUserDaoImpl implements IccUserDao {

    private final JdbcTemplate jdbcTemplate;

    public IccUserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Users> getUserByUsername(String username) {
        String sql = "SELECT * FROM IC_ADMIN.IC_USERS WHERE UPPER(USERNAME) = UPPER(?)";
        
        return jdbcTemplate.query(sql, new Object[]{username}, new UsersRowMapper());
    }

    private static class UsersRowMapper implements RowMapper<Users> {
        @Override
        public Users mapRow(ResultSet rs, int rowNum) throws SQLException {
            Users user = new Users();
            user.setUSERNAME(rs.getString("USERNAME"));
            user.setNAME(rs.getString("NAME"));
            user.setLAST_NAME(rs.getString("LAST_NAME"));
            user.setUSER_EMAIL(rs.getString("USER_EMAIL"));
            user.setGROUP_ID(rs.getInt("GROUP_ID"));
            user.setPROFILE_ID(rs.getInt("PROFILE_ID"));
            user.setLDAP_PROFILES(rs.getString("LDAP_PROFILES"));
            user.setEXPIRATION_DATE(rs.getDate("EXPIRATION_DATE"));
            return user;
        }
    }
}
