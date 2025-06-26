package com.wrapper.infrastructure.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.net.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;

public class Utils {

    public static final InetAddress INET_ADDRESS;

    static {
        InetAddress inetAddress = null;

        try {
            Enumeration<NetworkInterface> enumOfNetworkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (enumOfNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumOfNetworkInterfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp() || networkInterface.isVirtual()) {
                    continue;
                }

                Enumeration<InetAddress> addressEnumeration = networkInterface.getInetAddresses();

                while (addressEnumeration.hasMoreElements()) {
                    inetAddress = addressEnumeration.nextElement();

                    if (inetAddress instanceof Inet6Address) {
                        inetAddress = null;
                    }

                    if (inetAddress != null) {
                        break;
                    }
                }

                if (inetAddress != null) {
                    break;
                }
            }

            if (inetAddress == null) {
                inetAddress = InetAddress.getLocalHost();
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        INET_ADDRESS = inetAddress;
    }

    /**
     * Verifica se o objeto é nulo ou vazio.
     */
    public static boolean isEmpty(Object objToCheckIfEmpty) {
        if (objToCheckIfEmpty == null) {
            return true;
        }

        if (objToCheckIfEmpty instanceof String) {
            String objString = (String) objToCheckIfEmpty;

            if (objString.trim().isEmpty()) {
                return true;
            }
        }

        if (objToCheckIfEmpty instanceof Collection collectionToCheckIfEmpty) {

            if (collectionToCheckIfEmpty.isEmpty()) {
                return true;
            }
        }

        if (objToCheckIfEmpty instanceof Map) {
            Map c = (Map) objToCheckIfEmpty;

            if (c.isEmpty()) {
                return true;
            }
        }

        if (objToCheckIfEmpty.getClass().isArray()) {
            return Array.getLength(objToCheckIfEmpty) == 0;
        }

        return false;
    }

    public static Date getDateFromFormatter(Object input) {
        if (input == null) return null;

        if (input instanceof Date) return (Date) input;
        if (input instanceof OffsetDateTime) return Date.from(((OffsetDateTime) input).toInstant());

        if (input instanceof String) {
            String str = ((String) input).trim();
            Date date = tryParseUnixTimestamp(str);
            if (date != null) return date;

            return parseToDate(str);
        }

        return null;
    }

    public static <T> T getFromFormatter(String input, Class<T> clazz) {
        if (input == null || input.isBlank()) return null;

        String str = input.trim();
        Instant instant = tryParseInstant(str);
        if (instant == null) return null;

        if (clazz == Date.class) return (T) Date.from(instant);
        if (clazz == Instant.class) return (T) instant;
        if (clazz == OffsetDateTime.class) return (T) instant.atOffset(ZoneOffset.UTC);
        if (clazz == LocalDateTime.class) return (T) instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (clazz == LocalDate.class) return (T) instant.atZone(ZoneId.systemDefault()).toLocalDate();

        throw new IllegalArgumentException("Tipo não suportado: " + clazz);
    }

    private static Instant tryParseInstant(String input) {
        try {
            if (input.matches("\\d{13}")) {
                return Instant.ofEpochMilli(Long.parseLong(input));
            } else if (input.matches("\\d{10}")) {
                return Instant.ofEpochSecond(Long.parseLong(input));
            } else {
                return ZonedDateTime.parse(input, DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant();
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            // tenta outro formato
            try {
                return LocalDateTime.parse(input, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        .atZone(ZoneId.systemDefault())
                        .toInstant();
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private static Date tryParseUnixTimestamp(String input) {
        try {
            if (input.matches("\\d{13}")) {
                return new Date(Long.parseLong(input));
            } else if (input.matches("\\d{10}")) {
                return new Date(Long.parseLong(input) * 1000);
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private static Date parseToDate(String input) {
        try {
            Instant instant = tryParseInstant(input);
            return instant != null ? Date.from(instant) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Date offSetDateTimeToDate(OffsetDateTime offsetDateTime) {
        return offsetDateTime == null ? null : Date.from(offsetDateTime.toInstant());
    }

    /**
     * Verifica se o valor informado é um número.
     *
     * @param str o valor a ser verificado
     * @return {@code true} se o valor for um número
     */
    public static boolean isNumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }
        return str.matches("^-?[0-9]+(\\.[0-9]+)?$");
    }

    /**
     * Verifica se o valor informado é um inteiro.
     *
     * @param str o valor a ser verificado
     * @return {@code true} se o valor for um inteiro
     */
    public static boolean isInteger(String str) {
        if (isEmpty(str)) {
            return false;
        }
        return str.matches("^-?[0-9]+$");
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object val, T padrao) {
        if (val == null) return padrao;
        if (padrao == null) return (T) val;
        return cast((Class<T>) padrao.getClass(), val, padrao);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Class<T> clazz, Object val, T padrao) {
        if (val == null || clazz == null) return padrao;

        try {
            // Tipos compatíveis diretamente
            if (clazz.isInstance(val)) return (T) val;

            // Suporte a enums
            if (clazz.isEnum()) {
                String name = val.toString();
                for (T constant : clazz.getEnumConstants()) {
                    if (((Enum<?>) constant).name().equalsIgnoreCase(name)) {
                        return constant;
                    }
                }
                return padrao;
            }

            // Booleans
            if (clazz == Boolean.class || clazz == boolean.class) {
                String s = val.toString().toLowerCase();
                return (T) Boolean.valueOf(s.equals("true") || s.equals("1") || s.equals("yes") || s.equals("sim"));
            }

            // Numbers
            if (Number.class.isAssignableFrom(clazz) || clazz.isPrimitive()) {
                String s = val.toString().replace(",", ".");
                if (clazz == Integer.class || clazz == int.class) return (T) Integer.valueOf(s);
                if (clazz == Long.class || clazz == long.class) return (T) Long.valueOf(s);
                if (clazz == Double.class || clazz == double.class) return (T) Double.valueOf(s);
                if (clazz == Float.class || clazz == float.class) return (T) Float.valueOf(s);
                if (clazz == Short.class || clazz == short.class) return (T) Short.valueOf(s);
                if (clazz == Byte.class || clazz == byte.class) return (T) Byte.valueOf(s);
            }

            // Character
            if (clazz == Character.class || clazz == char.class) {
                String s = val.toString();
                return s.isEmpty() ? padrao : (T) Character.valueOf(s.charAt(0));
            }

            // String
            if (clazz == String.class) {
                return (T) val.toString();
            }

            // Datas básicas
            if (clazz == java.util.Date.class) {
                return (T) java.text.DateFormat.getInstance().parse(val.toString());
            }

            // Lista simples separada por vírgula
            if (clazz == List.class) {
                return (T) Arrays.asList(val.toString().split(","));
            }

            // Tentativa de usar construtor(String)
            try {
                Constructor<T> ctor = clazz.getConstructor(String.class);
                return ctor.newInstance(val.toString());
            } catch (NoSuchMethodException ignored) {
            }

        } catch (Exception e) {
            // Log/ignorado
        }

        return padrao;
    }

    public static void setValue(Map<String, Object> targetMap, String key, Object value) {
        setValue(targetMap, key, value, false, new HashMap<>(), new HashMap<>());
    }

    public static void setValue(Map<String, Object> mapOfColumnValues, String column, Object columnValue, boolean absent, Map<String, Map<String, Object>> mapOfColumnListNoIndex, Map<String, Map<String, Object>> mapOfNamedGroup) {
        Pair<String, Object> val = popularDados(mapOfColumnValues, columnValue, column.split("[.]"), mapOfColumnListNoIndex, mapOfNamedGroup);
        if (Objects.equals(val.getKey(), "") && val.getValue() instanceof Map) {
            Map<String, Object> value = (Map<String, Object>) val.getValue();
            if (absent) {
                value.forEach(mapOfColumnValues::putIfAbsent);
            } else {
                mapOfColumnValues.putAll(value);
            }
        } else {
            if (absent) {
                mapOfColumnValues.putIfAbsent(val.getKey(), val.getValue());
            } else {
                mapOfColumnValues.put(val.getKey(), val.getValue());
            }
        }

    }

    public static Pair<String, Object> popularDados(Map<String, Object> mapOfColumnValues, Object columnValue, String[] split, Map<String, Map<String, Object>> mapOfColumnListNoIndex, Map<String, Map<String, Object>> mapOfNamedGroup) {
        String columnName = split[0];
        boolean isList = false;
        Integer idxList = -1;
        String groupList = null;
        StringBuffer stringBuffer = new StringBuffer();

        if (columnName.contains("[")) {
            isList = true;
            Pattern pattern = Pattern.compile("\\[([0-9]+)\\]");
            Matcher matcher = pattern.matcher(columnName);

            if (matcher.find()) {
                idxList = Integer.parseInt(matcher.group(1));
            } else {
                idxList = null;
                pattern = Pattern.compile("\\[([a-zA-Z0-9\\-_]+)\\]");
                matcher = pattern.matcher(columnName);

                if (matcher.find()) {
                    groupList = matcher.group(1);
                } else {
                    pattern = Pattern.compile("\\[\\]");
                    matcher = pattern.matcher(columnName);
                    boolean matcherFound = matcher.find();
                }
            }

            matcher.appendReplacement(stringBuffer, "");
            matcher.appendTail(stringBuffer);

            columnName = stringBuffer.toString();
        }

        if (split.length == 1) {
            return Pair.of(columnName, columnValue);
        }

        Object anterior = mapOfColumnValues.get(columnName);

        if (anterior == null) {
            if (isList) {
                anterior = new LinkedList<>();
            } else {
                anterior = new HashMap<>();
            }
        }

        Object parent = null;

        if (isList) {
            List<Object> anteriorList = (List<Object>) anterior;

            if (idxList == null) {
                if (groupList == null) {
                    parent = getParentByGroupOrNoIndex(mapOfColumnListNoIndex, columnValue, split, groupList, anterior, anteriorList);
                } else {
                    parent = getParentByGroupOrNoIndex(mapOfNamedGroup, columnValue, split, groupList, anterior, anteriorList);
                }
            } else {
                if (split.length > 1) {
                    if (anteriorList.size() > idxList) {
                        parent = anteriorList.get(idxList);
                        if (parent == null) {
                            parent = new HashMap<>();
                            anteriorList.set(idxList, parent);
                        }
                    } else {
                        ajustarPosicaoArray(idxList, anteriorList);
                        parent = new HashMap<>();
                        anteriorList.add(idxList, parent);
                    }
                } else {
                    if (anteriorList.size() > idxList) {
                        anteriorList.set(idxList, columnValue);
                    } else {
                        ajustarPosicaoArray(idxList, anteriorList);
                        anteriorList.add(idxList, columnValue);
                    }
                    parent = anterior;
                }
            }
        } else {
            parent = anterior;
        }

        if (parent instanceof Map) {
            if (split.length > 1) {
                Pair<String, Object> val = popularDados((Map<String, Object>) parent, columnValue, Arrays.copyOfRange(split, 1, split.length), mapOfColumnListNoIndex, mapOfNamedGroup);
                if (Objects.equals(val.getKey(), "") && val.getValue() instanceof Map) {
                    Map<String, Object> value = (Map<String, Object>) val.getValue();
                    value.forEach(((Map) parent)::putIfAbsent);
                } else {
                    ((Map) parent).put(val.getKey(), val.getValue());
                }
            } else {
                ((Map) parent).put(columnName, columnValue);
            }
        }
        return Pair.of(columnName, anterior);
    }

    private static void ajustarPosicaoArray(Integer idxList, List<Object> anteriorList) {
        Integer idxTmp = null;
        while (anteriorList.size() <= idxList - 1) {
            if (idxTmp == null) {
                idxTmp = anteriorList.size();
            }
            anteriorList.add(idxTmp++, null);
        }
    }

    private static Object getParentByGroupOrNoIndex(Map<String, Map<String, Object>> mapaControle, Object columnValue, String[] split, String key, Object anterior, List<Object> anteriorList) {
        Object parent = null;

        if (!mapaControle.containsKey(key)) {
            if (split.length > 1) {
                parent = new LinkedHashMap<>();
                anteriorList.add(parent);
                mapaControle.put(key, (Map<String, Object>) parent);
            }
        } else {
            if (split.length > 1) {
                parent = mapaControle.get(key);
            }
        }

        return parent;
    }

    public static <K, V> void cloneMap(Map<K, V> origem, Map<K, V> destino) {
        if (origem == null || destino == null) {
            throw new IllegalArgumentException("Mapas não podem ser nulos");
        }
        destino.putAll(origem);
    }
}
