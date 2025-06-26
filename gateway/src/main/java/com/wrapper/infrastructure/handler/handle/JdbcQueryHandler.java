package com.wrapper.infrastructure.handler.handle;

import com.wrapper.infrastructure.handler.enuns.EnumJdbcFilterModifier;
import com.wrapper.infrastructure.handler.enuns.EnumJdbcFilterType;
import com.wrapper.infrastructure.handler.enuns.EnumJdbcPaginationStrategy;
import com.wrapper.infrastructure.handler.enuns.EnumJdbcSorterOrder;
import com.wrapper.infrastructure.handler.filter.JdbcFilter;
import com.wrapper.infrastructure.handler.filter.JdbcPageRequest;
import com.wrapper.infrastructure.handler.filter.JdbcSort;
import com.wrapper.infrastructure.handler.mapper.RowMapperResultSetExtractor;
import com.wrapper.infrastructure.handler.mapper.SQLParameterSource;
import com.wrapper.infrastructure.utils.JdbcUtils;
import com.wrapper.infrastructure.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.*;

@Component
public class JdbcQueryHandler extends AbstractJdbcQueryHandler {

    private static final Logger logger = LoggerFactory.getLogger(JdbcQueryHandler.class);

    public JdbcQueryHandler(DataSource dataSource) {
        super(dataSource);
    }

    public Object handleSQLQueryInternalOut(String sqlExpression, Map<String, Object> mapOfParams, NamedParameterJdbcOperations jdbcNamedOperations, JdbcPageRequest jdbcPageRequest, SQLParameterSource sqlParameterSource) {
        String qryToBeExecuted = null;
        String postgresQueryPlannerDisable = (String) mapOfParams.get("POSTGRES_QUERY_PLANNER_DISABLE");
        String[] arrOfParamsQueryPlanDisable = null;
        RuntimeException e = null;

        Object objQueryResultInternal = null;

        if (!Utils.isEmpty(postgresQueryPlannerDisable)) {
            arrOfParamsQueryPlanDisable = postgresQueryPlannerDisable.split(",");
        }

        try {
            if (!Utils.isEmpty(arrOfParamsQueryPlanDisable)) {
                for (String queryPlanner : arrOfParamsQueryPlanDisable) {
                    jdbcNamedOperations.update("SET " + queryPlanner + " TO OFF", mapOfParams);
                }
            }

            //verificando a estrategia de paginação que será aplicada
            EnumJdbcPaginationStrategy jdbcPaginationStrategy = EnumJdbcPaginationStrategy.DEFAULT;

            if (jdbcPaginationStrategy != null && jdbcPageRequest != null) {
                jdbcPaginationStrategy = jdbcPageRequest.getIntegradorJDBCPaginationStrategy();
            }

            StringBuilder sbQueryExpression = new StringBuilder();
            sbQueryExpression.append(sqlExpression);

            List<Map<String, Object>> listOfParamsFilters;
            List<Map<String, Object>> listOfParamsSorters = List.of();
            List<JdbcFilter> listOfJDBCFilters = new LinkedList<>();
            List<com.wrapper.infrastructure.handler.filter.JdbcSort> listOfJDBCSorters = new LinkedList<>();

            if (mapOfParams.containsKey("filters") && mapOfParams.get("filters") instanceof List<?>) {
                listOfParamsFilters = (List) mapOfParams.get("filters");

                for (Map mapOfFilterParams : listOfParamsFilters) {
                    String field = (String) mapOfFilterParams.get("field");
                    String modifierKey = (String) mapOfFilterParams.get("modifier");
                    String value = (String) mapOfFilterParams.get("value");
                    String typeKey = (String) mapOfFilterParams.get("type");
                    boolean checkNull = false;

                    if (Utils.isEmpty(field)) {
                        continue;
                    }

                    if (mapOfFilterParams.containsKey("check_null")) {
                        checkNull = (Boolean) mapOfFilterParams.get("check_null");
                    }

                    EnumJdbcFilterModifier modifier = EnumJdbcFilterModifier.EQ;

                    if (Utils.isEmpty(modifierKey)) {
                        modifier = EnumJdbcFilterModifier.valueOfKey(modifierKey);
                    }

                    EnumJdbcFilterType type = null;

                    if (Utils.isEmpty(typeKey)) {
                        type = EnumJdbcFilterType.valueOfKey(typeKey.trim().toUpperCase());
                    }

                    if (type == null) {
                        type = EnumJdbcFilterType.CHARACTER;
                    }

                    listOfJDBCFilters.add(new JdbcFilter(field, modifier, value, type, checkNull));

                }
            }

            if (mapOfParams.containsKey("sorters") && mapOfParams.get("sorters") instanceof List<?>
                    && jdbcPageRequest != null && jdbcPageRequest.isCounting()) {
                listOfParamsSorters = (List) mapOfParams.get("sorters");

                for (Map mapOfSorterParams : listOfParamsSorters) {
                    String field = (String) mapOfSorterParams.get("field");
                    String orderKey = (String) mapOfSorterParams.get("order");

                    if (Utils.isEmpty(field)) {
                        continue;
                    }

                    EnumJdbcSorterOrder order;

                    if (Utils.isEmpty(orderKey)) {
                        order = EnumJdbcSorterOrder.ASC;
                    } else {
                        order = EnumJdbcSorterOrder.valueOfKey(orderKey.trim().toUpperCase());
                    }

                    listOfJDBCSorters.add(new JdbcSort(field, order));
                }
            }

            if (!Utils.isEmpty(listOfJDBCFilters)) {
                sbQueryExpression = JdbcUtils.buildQueryWithListOfFilters(jdbcPaginationStrategy, sbQueryExpression, listOfJDBCFilters, sqlParameterSource);
            }

            if (!Utils.isEmpty(listOfJDBCFilters)) {
                com.wrapper.infrastructure.utils.JdbcUtils.buildQueryWithListOfSorters(sbQueryExpression, listOfJDBCSorters);
            }

            qryToBeExecuted = sbQueryExpression.toString();

            if (jdbcPageRequest != null
                    && (jdbcPageRequest.isCounting() || jdbcPageRequest.isPageable())) {
                String paramPageTemplate = "SQL_" + jdbcPaginationStrategy.name();

                if (jdbcPageRequest.isCounting()) {
                    //bloco abaixo para realizar o COUNT dos registros da QUERY
                    paramPageTemplate += "_COUNTING";
                } else if (jdbcPageRequest.isPageable()) {
                    //bloco abaixo para realizar a PAGINACAO dos registros da QUERY
                    paramPageTemplate += "_PAGINATOR";
                }

                paramPageTemplate += "_TEMPLATE";
                String qryPaginatorTemplate = (String) mapOfParams.getOrDefault(paramPageTemplate, "");

                qryToBeExecuted = qryPaginatorTemplate.replace(":query", qryToBeExecuted);

                if (!Utils.isEmpty(listOfJDBCFilters)) {
                    qryToBeExecuted = qryToBeExecuted.replace("tmp.rownumber", "tmp.rownumber_filter");
                }

                sqlParameterSource.putValue("page", jdbcPageRequest.getPage());
                sqlParameterSource.putValue("size", jdbcPageRequest.getSize());
                sqlParameterSource.putValue("offset", jdbcPageRequest.getOffset());
                sqlParameterSource.putValue("first", jdbcPageRequest.getFirst());
                sqlParameterSource.putValue("last", jdbcPageRequest.getLast());

            }

            logger.info("Starting JDBC Gateway");

            if (jdbcPageRequest != null) {
                if (jdbcPageRequest.isCounting()) {
                    objQueryResultInternal = jdbcNamedOperations.queryForObject(qryToBeExecuted, sqlParameterSource, new ColumnMapRowMapper());
                } else if (jdbcPageRequest.isPageable()) {
                    objQueryResultInternal = jdbcNamedOperations.query(qryToBeExecuted, sqlParameterSource, new RowMapperResultSetExtractor(true));
                } else {
                    objQueryResultInternal = null;
                }
            } else {
                objQueryResultInternal = jdbcNamedOperations.query(qryToBeExecuted, sqlParameterSource, new RowMapperResultSetExtractor());
            }

            return objQueryResultInternal;
        } catch (Exception err) {

            StringBuilder sbSQLError = new StringBuilder();
            sbSQLError.append("\nErro ao executar o JdbcGateway").append("\n");
            sbSQLError.append("Exception: ").append(err.getMessage()).append("\n");

            if (err.getCause() != null) {
                sbSQLError.append("Cause: ").append(err.getCause().getMessage()).append("\n");
            }

            e = new RuntimeException(sbSQLError.toString(), err);
        } finally {
            if (e == null
                    && !Utils.isEmpty(arrOfParamsQueryPlanDisable)) {
                for (String queryPlanner : arrOfParamsQueryPlanDisable) {
                    jdbcNamedOperations.update("SET " + queryPlanner + " TO ON", mapOfParams);
                }
            } else if (e != null) {
                throw e;
            }
        }

        return objQueryResultInternal;
    }

}
