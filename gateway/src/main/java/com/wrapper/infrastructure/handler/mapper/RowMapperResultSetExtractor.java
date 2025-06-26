package com.wrapper.infrastructure.handler.mapper;

import com.wrapper.infrastructure.utils.Utils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RowMapperResultSetExtractor implements ResultSetExtractor<List<Map<String, Object>>> {

    private ColumnMapRowMapper columnMapRowMapper;

    public RowMapperResultSetExtractor() {
        this(false);
    }

    public RowMapperResultSetExtractor(boolean trimCharValues) {
        columnMapRowMapper = new ColumnMapRowMapper();
    }

    @Override
    public List<Map<String, Object>> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<Map<String, Object>> results = new LinkedList<>();
        int rowNumber = 0;
        while (rs.next()) {
            results.add(columnMapRowMapper.mapRow(rs, rowNumber++));
        }
        if (!Utils.isEmpty(results)) {
            return results;
        }
        return List.of();
    }
}
