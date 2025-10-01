package com.claropr.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class SirDatabaseConfig {

    @Value("${sir.database.url}")
    private String sirDatabaseUrl;

    @Value("${sir.database.username}")
    private String sirDatabaseUsername;

    @Value("${sir.database.password}")
    private String sirDatabasePassword;

    @Bean(name = "sirDataSource")
    public DataSource sirDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        dataSource.setUrl(sirDatabaseUrl);
        dataSource.setUsername(sirDatabaseUsername);
        dataSource.setPassword(sirDatabasePassword);
        return dataSource;
    }

    @Bean(name = "sirJdbcTemplate")
    public JdbcTemplate sirJdbcTemplate(@Qualifier("sirDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
