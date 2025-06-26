package com.wrapper.mvc.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Configuration(proxyBeanMethods = false)
public class DatasourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DatasourceConfig.class);

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();

        ds.setJdbcUrl(env.getRequiredProperty("spring.datasource.url"));
        ds.setUsername(env.getRequiredProperty("spring.datasource.username"));
        ds.setPassword(env.getRequiredProperty("spring.datasource.password"));
        ds.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));

        ds.setMaximumPoolSize(env.getProperty("spring.datasource.hikari.maximum-pool-size", Integer.class, 10));
        ds.setMinimumIdle(env.getProperty("spring.datasource.hikari.minimum-idle", Integer.class, 2));
        ds.setIdleTimeout(env.getProperty("spring.datasource.hikari.idle-timeout", Long.class, 60000L));
        ds.setMaxLifetime(env.getProperty("spring.datasource.hikari.max-lifetime", Long.class, 1800000L));
        ds.setConnectionTimeout(env.getProperty("spring.datasource.hikari.connection-timeout", Long.class, 30000L));

        testConnection(ds);

        return ds;
    }

    public void testConnection(DataSource currentDataSource) {
        int tentativas = 0;
        while (tentativas <= 10) {
            try {
                Connection connection = currentDataSource.getConnection();
                if (connection.isValid(2)) {
                    log.info("Conexao bem sucedida com [{}]", connection.getMetaData().getURL());
                    break;
                } else {
                    log.error("Conexao mal sucedida com [{}] tentando novamente", connection.getMetaData().getURL());
                    currentDataSource = this.dataSource();
                    tentativas++;
                }
            } catch (SQLException e) {
                log.error("erro ao estabeleces conexao, motivo:\n" + e.getCause());
                break;
            }
        }
    }

    @PostConstruct
    public void disableHikariLogs() {
        for (String name : List.of(
                "com.zaxxer.hikari.pool.HikariPool",
                "com.zaxxer.hikari.pool.PoolBase",
                "com.zaxxer.hikari.HikariConfig",
                "com.zaxxer.hikari.HikariDataSource")) {
            ((Logger) LoggerFactory.getLogger(name)).isEnabledForLevel(Level.ERROR);
        }
    }
}
