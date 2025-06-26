package com.wrapper.infrastructure.handler.enuns;

public interface IEnum<E> {
    E getKey();

    String getValue();

    static <E, X extends IEnum<E>> X valueOfKey(Class<X> e, E key) {
        return valueOfKey(e, key, false);
    }

    static <E, X extends IEnum<E>> X valueOfKey(Class<X> e, E key, boolean ignoreCase) {
        if (key == null) {
            return null;
        }
        if (!e.isEnum()) {
            throw new IllegalArgumentException("Classe não eh ENUM");
        }

        X[] arrOfIEnum = e.getEnumConstants();

        for (X iEnum : arrOfIEnum) {
            if (!ignoreCase) {
                if (iEnum.getKey().equals(key)) {
                    return iEnum;
                }
                if (((Enum) iEnum).name().equals(key)) {
                    return iEnum;
                }
            } else {
                if (iEnum.getKey().toString().equalsIgnoreCase(key.toString())) {
                    return iEnum;
                }
                if (((Enum) iEnum).name().equalsIgnoreCase(key.toString())) {
                    return iEnum;
                }
            }
        }

        return null;
    }
}
