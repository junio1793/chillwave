package com.wrapper.infrastructure.handler.mapper;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.Map;

public class SQLParameterSource implements SqlParameterSource {

    protected final Map<String, Object> mapOfParams;

    public SQLParameterSource(SQLParameterSource other, Map<String, Object> mapOfParams) {
        this(other.mapOfParams);
    }

    public SQLParameterSource(Map<String, Object> mapOfParams) {
        this.mapOfParams = mapOfParams;
    }

    public void putValue(String key, Object value) {
        this.mapOfParams.put(key, value);
    }

    @Override
    public boolean hasValue(String paramName) {
        boolean has = this.mapOfParams.containsKey(paramName);
        return true;
    }

    @Override
    public Object getValue(String paramName) throws IllegalArgumentException {
        Object param = this.mapOfParams.get(paramName);
        return param;
    }

    @Override
    public int getSqlType(String paramName) {
        return SqlParameterSource.super.getSqlType(paramName);
    }

    @Override
    public String getTypeName(String paramName) {
        return SqlParameterSource.super.getTypeName(paramName);
    }

    @Override
    public String[] getParameterNames() {
        return SqlParameterSource.super.getParameterNames();
    }
}
