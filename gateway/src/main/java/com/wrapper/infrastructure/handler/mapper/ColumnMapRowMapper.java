package com.wrapper.infrastructure.handler.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wrapper.infrastructure.utils.JsonObjectMapper;
import com.wrapper.infrastructure.utils.Utils;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ColumnMapRowMapper implements RowMapper<Map<String, Object>> {

    @Autowired
    private ApplicationContext applicationContext;

    private JsonObjectMapper jsonObjectMapper = JsonObjectMapper.jsonObjectMapper();

    private boolean trimCharValues = false;

    public ColumnMapRowMapper(boolean trimCharValues) {
        this.trimCharValues = trimCharValues;
    }

    public ColumnMapRowMapper() {
    }

    @Override
    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        Map<String, Object> mapOfColumnValues = new LinkedHashMap<>();
        Map<String, Map<String, Object>> mapOfColumnListNoIndex = new LinkedHashMap<>();
        Map<String, Map<String, Object>> mapOfNamedGroup = new LinkedHashMap<>();

        for (int i = 1; i <= columnCount; i++) {
            String[] columns = new String[]{getColumnKey(JdbcUtils.lookupColumnName(rsmd, i))};
            Object columnValue = null;

            try {
                columnValue = getColumnValue(rs, i, columns);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            if (trimCharValues && columnValue instanceof String) {
                columnValue = ((String) columnValue).trim();
            }

            if (columns[0].contains("|")) {
                columns = columns[0].split("[|]");
            }

            for (String column : columns) {
                Utils.setValue(mapOfColumnValues, column, columnValue, true, mapOfColumnListNoIndex, mapOfNamedGroup);
            }
        }
        return mapOfColumnValues;
    }

    /**
     * Retrieve a JDBC object value for the specified column.
     * <p>
     * The default implementation uses the {@code getObject} method.
     * Additionally, this implementation includes a "hack" to get around Oracle
     * returning a non-standard object for their TIMESTAMP datatype.
     *
     * @param rs    is the ResultSet holding the data
     * @param index is the column index
     * @return the Object returned
     * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue
     */
    private Object getColumnValue(ResultSet rs, int index, String[] columns) throws SQLException, JsonProcessingException {
        Object columnValue = JdbcUtils.getResultSetValue(rs, index);

        if (columnValue instanceof Array) {
            Object[] array = (Object[]) ((Array) columnValue).getArray();
            columnValue = Stream.of(array).collect(Collectors.toCollection(LinkedList::new));
        }

        if (columnValue instanceof PGobject) {
            PGobject po = (PGobject) columnValue;
            String pgRawType = po.getType();

            if (pgRawType.equalsIgnoreCase("json")
                    || pgRawType.equalsIgnoreCase("jsonb")) {
                String jsonValueOnBD = po.getValue();

                Optional<String> hasJsonEndColumnWildcard = Arrays.stream(columns)
                        .filter(Objects::nonNull)
                        .filter(s -> s.contains("_json|") || s.endsWith("_json"))
                        .findFirst();

                if (!hasJsonEndColumnWildcard.isPresent()) {
                    if (jsonValueOnBD.startsWith("[")
                            || jsonValueOnBD.startsWith("{")) {
                        columnValue = jsonObjectMapper.readValue(jsonValueOnBD, Object.class);
                    } else if (Utils.isNumeric(jsonValueOnBD)) {
                        if (Utils.isInteger(jsonValueOnBD)) {
                            columnValue = Utils.cast(jsonValueOnBD, Integer.class);
                        } else {
                            columnValue = Utils.cast(jsonValueOnBD, BigDecimal.class);
                        }
                    } else {
                        columnValue = jsonValueOnBD;
                    }
                }
            }
        }

        return columnValue;
    }

    /**
     * Determine the key to use for the given column in the column Map.
     *
     * @param columnName the column name as returned by the ResultSet
     * @return the column key to use
     * @see java.sql.ResultSetMetaData#getColumnName
     */
    protected String getColumnKey(String columnName) {
        return columnName;
    }

}
