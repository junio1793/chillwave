package com.wrapper.infrastructure.handler.enuns;

public enum EnumJdbcFilterType implements IEnum<String>{

    CHARACTER("CHARACTER", "Character"),
    NUMERIC("NUMERIC", "Numeric"),
    NUMBER("NUMBER", "Number"),
    INTEGER("INTEGER", "Integer"),
    FLOAT("FLOAT", "Float"),
    DATE("DATE", "Date/Timestamp"),
    BOOLEAN("BOOLEAN", "Boolean");

    private final String key;
    private final String value;

    EnumJdbcFilterType(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public static EnumJdbcFilterType valueOfKey(String key) {
        EnumJdbcFilterType[] arrOfEnumConstants = EnumJdbcFilterType.class.getEnumConstants();

        for (EnumJdbcFilterType enumConstant : arrOfEnumConstants) {
            if (enumConstant.getKey().equals(key)) {
                return enumConstant;
            }
        }

        return null;
    }
}
