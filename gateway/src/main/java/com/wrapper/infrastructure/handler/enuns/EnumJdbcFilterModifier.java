package com.wrapper.infrastructure.handler.enuns;

public enum EnumJdbcFilterModifier implements IEnum<String> {

    EQ("EQ", "Equal", "="),
    NE("NE", "Not Equal", "<>"),
    IN("IN", "In list of values", "IN"),
    NOT_IN("NOT_IN", "Not in list of values", "NOT IN"),
    LIKE("LIKE", "Like", "LIKE"),
    NOT_LIKE("NOT_LIKE", "Not Like", "NOT LIKE"),
    GT("GT", "Greater than", ">"),
    LT("LT", "Lesser than", "<"),
    GE("GE", "Greater or equal than", ">="),
    LE("LE", "Lesser or equal than", "<="),
    IS_NULL("IS_NULL", "Is null", "IS NULL"),
    IS_NOT_NULL("IS_NOT_NULL", "Is not null", "IS NOT NULL");

    private final String key;
    private final String value;
    private final String operator;

    EnumJdbcFilterModifier(String key, String value, String operator) {
        this.key = key;
        this.value = value;
        this.operator = operator;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getOperator() {
        return operator;
    }

    public static EnumJdbcFilterModifier valueOfKey(String key) {
        EnumJdbcFilterModifier[] arrOfEnumConstants = EnumJdbcFilterModifier.class.getEnumConstants();

        for (EnumJdbcFilterModifier enumConstant : arrOfEnumConstants) {
            if (enumConstant.getKey().equals(key)) {
                return enumConstant;
            }
        }

        return null;
    }
}
