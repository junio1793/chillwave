package com.wrapper.infrastructure.handler.enuns;

public enum EnumJdbcSorterOrder implements IEnum<String>{

    ASC("ASC", "Ascendant"),
    DESC("DESC", "Descendant");

    private final String key;
    private final String value;

    EnumJdbcSorterOrder(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public static EnumJdbcSorterOrder valueOfKey(String key) {
        EnumJdbcSorterOrder[] arrOfEnumConstants = EnumJdbcSorterOrder.class.getEnumConstants();

        for (EnumJdbcSorterOrder enumConstant : arrOfEnumConstants) {
            if (enumConstant.getKey().equals(key)) {
                return enumConstant;
            }
        }

        return null;
    }
}
