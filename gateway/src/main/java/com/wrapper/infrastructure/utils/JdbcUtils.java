package com.wrapper.infrastructure.utils;

import com.wrapper.infrastructure.handler.enuns.EnumJdbcFilterModifier;
import com.wrapper.infrastructure.handler.enuns.EnumJdbcFilterType;
import com.wrapper.infrastructure.handler.enuns.EnumJdbcPaginationStrategy;
import com.wrapper.infrastructure.handler.filter.JdbcFilter;
import com.wrapper.infrastructure.handler.filter.JdbcSort;
import jakarta.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JdbcUtils {

    public static StringBuilder buildQueryWithListOfFilters(EnumJdbcPaginationStrategy enumJdbcPaginationStrategy, StringBuilder sbBaseQuery, List<JdbcFilter> filters, Object objectJDBCFiltersParams) {
        String baseAlias = "qry";

        StringBuilder sbQueryWithFilters = new StringBuilder();
        sbQueryWithFilters.append("SELECT ").append(baseAlias).append(".*");

        if (enumJdbcPaginationStrategy == EnumJdbcPaginationStrategy.ROWNUMBER) {
            sbQueryWithFilters.append("   , DENSE_RANK() OVER (ORDER BY qry.rownumber) rownumber_filter");
        }

        sbQueryWithFilters.append("  FROM (").append(sbBaseQuery).append(") ").append(baseAlias);
        sbQueryWithFilters.append(" WHERE 1 = 1");

        AtomicInteger idxFilter = new AtomicInteger(1);

        filters.forEach(testeJDBCFilter -> {
            addQueryParameter(sbQueryWithFilters, baseAlias, testeJDBCFilter, idxFilter.getAndIncrement(), objectJDBCFiltersParams);
        });

        sbQueryWithFilters.append("   AND 1 = 1");
        return sbQueryWithFilters;
    }

    private static void addQueryParameter(StringBuilder sbBaseQuery, String baseAlias, JdbcFilter testeJDBCFilter, int andIncrement, Object objectJDBCFiltersParams) {
        String aliasFilter = "filter_" + andIncrement;

        EnumJdbcFilterType enumJDBCFilterType = testeJDBCFilter.getType();
        EnumJdbcFilterModifier enumJDBCFilterModifier = testeJDBCFilter.getModifier();

        String filterField = testeJDBCFilter.getField();

        if (filterField.matches("^(?=.*[A-Z,.])(?=.*[a-z,.]).+$")) {
            filterField = "\"" + filterField + "\"";
        }

        boolean isNorOrNotNull = (enumJDBCFilterModifier == EnumJdbcFilterModifier.IS_NULL || enumJDBCFilterModifier == EnumJdbcFilterModifier.IS_NOT_NULL);

        if (isNorOrNotNull) {
            sbBaseQuery.append(" AND ( ")
                    .append(baseAlias)
                    .append(".")
                    .append(filterField)
                    .append(" ")
                    .append(enumJDBCFilterModifier.getOperator())
                    .append(" )");
        } else {
            sbBaseQuery.append(" AND ( ")
                    .append(baseAlias)
                    .append(".")
                    .append(filterField)
                    .append(" ")
                    .append(enumJDBCFilterModifier.getOperator());

            if (objectJDBCFiltersParams instanceof List) {
                sbBaseQuery.append(" ?");
            } else if (objectJDBCFiltersParams instanceof Map) {
                sbBaseQuery.append(" :").append(aliasFilter);
            }

            if (testeJDBCFilter.isCheckNull()) {
                sbBaseQuery.append(" OR ")
                        .append(baseAlias)
                        .append(".")
                        .append(testeJDBCFilter.getField())
                        .append(" IS NULL");
            }

            sbBaseQuery.append(" )");

            Object objFilterValue;

            switch (enumJDBCFilterType) {
                case DATE:
                    objFilterValue = Utils.getDateFromFormatter(testeJDBCFilter.getValue());

                    break;
                case BOOLEAN:
                    if (testeJDBCFilter.getValue() == null) {
                        objFilterValue = Boolean.FALSE;
                    } else {
                        objFilterValue = Boolean.valueOf(testeJDBCFilter.getValue());
                    }

                    break;
                case INTEGER:
                case NUMERIC:
                    objFilterValue = Integer.valueOf(testeJDBCFilter.getValue());

                    break;
                case NUMBER:
                case FLOAT:
                    objFilterValue = BigDecimal.valueOf(Long.valueOf(testeJDBCFilter.getValue()));

                    break;
                default:
                    objFilterValue = testeJDBCFilter.getValue();
            }

            if (objectJDBCFiltersParams instanceof List) {
                ((List) objectJDBCFiltersParams).add(objFilterValue);
            } else if (objectJDBCFiltersParams instanceof Map) {
                ((Map) objectJDBCFiltersParams).put(aliasFilter, objFilterValue);
            }
        }
    }

    public static void buildQueryWithListOfSorters(StringBuilder sbBaseQuery, List<JdbcSort> listOfHorizonJDBCSorters) {
        AtomicInteger idxFilter = new AtomicInteger(1);

        listOfHorizonJDBCSorters
                .forEach(horizonJDBCSort -> {
                    if (idxFilter.getAndAdd(1) == 1) {
                        sbBaseQuery.append(" ORDER BY ");
                    } else {
                        sbBaseQuery.append(", ");
                    }

                    sbBaseQuery.append(horizonJDBCSort.getField());

                    if (horizonJDBCSort.getOrder() != null) {
                        sbBaseQuery.append(" ").append(horizonJDBCSort.getOrder().getKey());
                    }
                });
    }

    public static Map<String, Object> buildMapOfQueryParameters(HttpServletRequest httpServletRequest) {
        if (httpServletRequest == null) {
            return null;
        }

        Map<String, Object> mapOfQueryParams = new LinkedHashMap<>();
        String queryParam = httpServletRequest.getQueryString();

        if (!queryParam.contains("&")) {
            return null;
        }

        String[] splitQueryparams = queryParam.split("&");

        for (String param : splitQueryparams) {
            String[] arrOfKeyValuePair = param.split("=");
            String paramKey;
            String paramValue;
            String valueStr = arrOfKeyValuePair.length == 1 ? "" : arrOfKeyValuePair[1];

            try {
                paramKey = URLEncoder.encode(arrOfKeyValuePair[0], "UTF-8");
                paramValue = URLEncoder.encode(valueStr, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                paramKey = arrOfKeyValuePair[0];
                paramValue = valueStr;
            }

            Pattern pattern = Pattern.compile("\\b(?:size|page)\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(paramKey);

            if (matcher.find()) {
                Integer paramValueInt = Integer.valueOf(paramValue);
                mapOfQueryParams.put(paramKey, paramValueInt);
            } else {
                mapOfQueryParams.put(paramKey, paramValue);
            }

        }

        return mapOfQueryParams;
    }

    public static Map<String, Object> buildMapOfPathParameters(HttpServletRequest httpServletRequest) {
        if (httpServletRequest == null) {
            return null;
        }

        Map<String, Object> mapOfPathParameters = new HashMap<>();
        String strRequestURI = httpServletRequest.getRequestURI();

        if (strRequestURI.contains("/jdbcgateway/")) {
            String[] splitURLPath = strRequestURI.split("/jdbcgateway/");
            splitURLPath = splitURLPath[1].split("/");

            if (splitURLPath.length == 2) {
                String constante = splitURLPath[0];
                String identify = splitURLPath[1];

                try {
                    constante = URLEncoder.encode(constante, "UTF-8");
                    identify = URLEncoder.encode(identify, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    //ignore
                }
                mapOfPathParameters.put("endpoint_constante", constante);
                mapOfPathParameters.put("endpoint_identify", identify);
            }
        }

        return mapOfPathParameters;
    }
}
