package com.wrapper.infrastructure.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Service("endpointQueryConfigTable")
public class EndpointQueryConfigTable implements IEndpointQueryConfigTable {

    private static Logger logger = LoggerFactory.getLogger(EndpointQueryConfigTable.class);

    private final JdbcTemplate jdbcTemplate;
    private static final String SCHEMA_NAME = "public";
    private static final String TABLE_NAME = "endpoint_query_config";

    public EndpointQueryConfigTable(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Cacheable(cacheNames = "endpointQueryConfig", key = "#constante + ':' + #identify")
    public Map find(String constante, String identify) {
        logger.info("SELECT ENDPOINT {}", constante + "/" + identify);
        Map result = new LinkedHashMap<>();
        try {
            String sql = "SELECT * FROM endpoint_query_config WHERE constante = ? AND unique_identify = ?";
            result = jdbcTemplate.queryForMap(sql, constante, identify);
        } catch (Exception e) {
            if (e instanceof EmptyResultDataAccessException) {
                return Collections.emptyMap();
            }
        }
        return result;
    }

    public boolean exists() {
        String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = ? AND table_name = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, SCHEMA_NAME, TABLE_NAME) > 0;
    }

    public void create() {
        String sql = "CREATE TABLE IF NOT EXISTS " + SCHEMA_NAME + "." + TABLE_NAME + " (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(255), " +
                "body TEXT, " +
                "query_planner VARCHAR(255), " +
                "constante VARCHAR(255), " +
                "unique_identify VARCHAR(255), " +
                "version VARCHAR(50) DEFAULT '1-SNAPSHOT', " +
                "ativo BOOLEAN, " +
                "dh_registro TIMESTAMP, " +
                "dh_atualizacao TIMESTAMP, " +
                "JDBC_PAGINATION_STRATEGY VARCHAR(10))";
        jdbcTemplate.execute(sql);
        logger.info("CREATE TABLE IF NOT EXISTS {}.{} IN DATABASE", SCHEMA_NAME, TABLE_NAME);
    }

    public void drop() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + SCHEMA_NAME + "." + TABLE_NAME);
    }

    public void truncate() {
        jdbcTemplate.execute("TRUNCATE TABLE " + SCHEMA_NAME + "." + TABLE_NAME);
    }
}
