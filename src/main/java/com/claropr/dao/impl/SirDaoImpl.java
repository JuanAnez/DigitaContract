package com.claropr.dao.impl;

import com.claropr.dao.SirDao;
import com.claropr.model.LovItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SirDaoImpl implements SirDao {

    @Autowired
    @Qualifier("sirJdbcTemplate")
    private JdbcTemplate sirJdbcTemplate;

    @Override
    public List<LovItem> getOficinasComerciales() {
        String sql = "SELECT VALUE_DESC, LOV_ID FROM SIR_APP.LOV " +
                    "WHERE LOV_MODULE = 'OFICINASCOMERCIALES' " +
                    "AND EXPIRATION_DATE IS NULL " +
                    "ORDER BY VALUE_DESC";
        
        return sirJdbcTemplate.query(sql, new LovItemRowMapper());
    }

    private static class LovItemRowMapper implements RowMapper<LovItem> {
        @Override
        public LovItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new LovItem(
                rs.getString("VALUE_DESC"),
                rs.getString("LOV_ID")
            );
        }
    }
}
