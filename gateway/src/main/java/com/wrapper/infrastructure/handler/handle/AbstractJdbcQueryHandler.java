package com.wrapper.infrastructure.handler.handle;

import com.wrapper.infrastructure.endpoint.IEndpointQueryConfigTable;
import com.wrapper.infrastructure.handler.enuns.EnumJdbcPaginationStrategy;
import com.wrapper.infrastructure.handler.filter.JdbcPageRequest;
import com.wrapper.infrastructure.handler.mapper.SQLParameterSource;
import com.wrapper.infrastructure.utils.JdbcUtils;
import com.wrapper.infrastructure.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.*;

public abstract class AbstractJdbcQueryHandler implements IJdbcQueryHandle {

    private static final Logger logger = LoggerFactory.getLogger(AbstractJdbcQueryHandler.class);

    @Autowired
    protected NamedParameterJdbcOperations jdbcNamedOperations;

    @Autowired
    protected IEndpointQueryConfigTable iEndpointQueryConfigTable;

    protected DataSource dataSource;

    public AbstractJdbcQueryHandler(DataSource dataSource) {
        this.jdbcNamedOperations = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Object handleQueryInternal(DataSource currentDataSource, HttpServletRequest httpServletRequest) {
        this.jdbcNamedOperations = new NamedParameterJdbcTemplate(currentDataSource);
        this.dataSource = currentDataSource;
        return handleSQLQueryInternal(jdbcNamedOperations, httpServletRequest);
    }

    public Object handleSQLQueryInternal(NamedParameterJdbcOperations jdbcNamedOperations, HttpServletRequest httpServletRequest) {
        Map<String, Object> mapOfQueryParametersHandleded = JdbcUtils.buildMapOfQueryParameters(httpServletRequest);
        Map<String, Object> newmapOfQueryParametersHandleded = JdbcUtils.buildMapOfPathParameters(httpServletRequest);

        JdbcPageRequest jdbcPageRequestResult = null;
        EnumJdbcPaginationStrategy enumJdbcPaginationStrategy = EnumJdbcPaginationStrategy.DEFAULT;

        SQLParameterSource sqlParameterSource = null;

        Map mapEndpointConfig = new LinkedHashMap();
        String sqlExpression = "";

        if (newmapOfQueryParametersHandleded.containsKey("endpoint_constante")
                && newmapOfQueryParametersHandleded.containsKey("endpoint_identify")) {
            String constante = (String) newmapOfQueryParametersHandleded.get("endpoint_constante");
            String indentify = (String) newmapOfQueryParametersHandleded.get("endpoint_identify");

            boolean exists = iEndpointQueryConfigTable.exists();

            if (exists) {
                mapEndpointConfig = iEndpointQueryConfigTable.find(constante, indentify);
            } else {
                iEndpointQueryConfigTable.create();
            }
        }

        if (Utils.isEmpty(mapEndpointConfig)) {
            return Collections.EMPTY_LIST;
        }

        sqlExpression = (String) mapEndpointConfig.get("body");
        enumJdbcPaginationStrategy = (mapEndpointConfig.containsKey("JDBC_PAGINATION_STRATEGY") && mapEndpointConfig.get("JDBC_PAGINATION_STRATEGY") != null ?
                EnumJdbcPaginationStrategy.valueOf(mapEndpointConfig.get("JDBC_PAGINATION_STRATEGY").toString()) : EnumJdbcPaginationStrategy.DEFAULT);

        SQLParameterSource SQLParameterSource = new SQLParameterSource(mapOfQueryParametersHandleded);

        if (Utils.isEmpty(enumJdbcPaginationStrategy)) {
            int page = 0;
            int size = 0;
            int maxSize = 0;
            int offset = 0;
            int first = 0;
            int last = 0;

            page = Utils.cast(mapOfQueryParametersHandleded.get("page"), 1);
            size = Utils.cast(mapOfQueryParametersHandleded.get("size"), 100);
            maxSize = Utils.cast(mapOfQueryParametersHandleded.get("maxPageSize"), 100);
            offset = Utils.cast(mapOfQueryParametersHandleded.get("offset"), 0);
            first = Utils.cast(mapOfQueryParametersHandleded.get("first"), 0);
            last = Utils.cast(mapOfQueryParametersHandleded.get("last"), 0);

            jdbcPageRequestResult.setCounting(false);
            jdbcPageRequestResult.setPageable(false);
            jdbcPageRequestResult.setIntegradorJDBCPaginationStrategy(enumJdbcPaginationStrategy);

            if (page <= 0) {
                page = 1;
            }

            if (first <= 0) {
                first = (page * size) - size;
            }

            if (last <= 0) {
                last = (page * size);
            }

            if (offset == 0) {
                offset = (page * size) - size;
            }

            if (offset < 0) {
                offset = 0;
            }

            jdbcPageRequestResult.setPage(page);
            jdbcPageRequestResult.setSize(size);
            jdbcPageRequestResult.setMaxSize(maxSize);
            jdbcPageRequestResult.setOffset(offset);
            jdbcPageRequestResult.setFirst(first);
            jdbcPageRequestResult.setLast(last);
        }

        Utils.cloneMap(mapOfQueryParametersHandleded, newmapOfQueryParametersHandleded);

        Object objResults = handleSQLQueryInternalOut(sqlExpression, newmapOfQueryParametersHandleded, jdbcNamedOperations, jdbcPageRequestResult, SQLParameterSource);

        if (!Utils.isEmpty(objResults)) {
            return objResults;
        }

        return new ArrayList<>();
    }

    protected abstract Object handleSQLQueryInternalOut(String sqlExpression,
                                                        Map<String, Object> mapOfQueryParametersHandleded,
                                                        NamedParameterJdbcOperations jdbcNamedOperations,
                                                        JdbcPageRequest jdbcPageRequest,
                                                        SQLParameterSource sqlParameterSource);
}
