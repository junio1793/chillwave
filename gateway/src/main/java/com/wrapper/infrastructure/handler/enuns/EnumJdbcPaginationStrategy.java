package com.wrapper.infrastructure.handler.enuns;

public enum EnumJdbcPaginationStrategy implements IEnum<String>{

    DEFAULT("DEFAULT", "O valor refere-se a paginação default."),
    ROWNUMBER("ROWNUMBER", "O valor refere-se a paginação rownumber.");

    private final String key;
    private final String value;

    EnumJdbcPaginationStrategy(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
