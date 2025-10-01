package com.claropr.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {
    // DataSource configuration is now handled in contract-servlet.xml
    // This matches the original project's approach using JndiObjectFactoryBean
}
