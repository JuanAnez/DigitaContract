package com.claropr.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class IcAdminDatabaseConfig {

    @Value("${icadmin.database.url}")
    private String url;

    @Value("${icadmin.database.username}")
    private String username;

    @Value("${icadmin.database.password}")
    private String password;

    @Value("${icadmin.database.driver-class-name}")
    private String driverClassName;

    @Bean(name = "icAdminDataSource")
    public DataSource icAdminDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        return dataSource;
    }

    @Bean(name = "icAdminJdbcTemplate")
    public JdbcTemplate icAdminJdbcTemplate(@Qualifier("icAdminDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
