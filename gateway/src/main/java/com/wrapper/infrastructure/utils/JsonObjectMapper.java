package com.wrapper.infrastructure.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class JsonObjectMapper extends ObjectMapper implements Serializable {

    private static final long serialVersionUID = -1093849448573802804L;

    private JsonObjectMapper(JsonFactory jsonFactory) {
        super(jsonFactory);

        SimpleModule simpleModule = new SimpleModule("Types");
        simpleModule.addAbstractTypeMapping(Map.class, LinkedHashMap.class);
        simpleModule.addAbstractTypeMapping(List.class, LinkedList.class);
        simpleModule.addAbstractTypeMapping(Set.class, LinkedHashSet.class);

        registerModule(simpleModule);

        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, true);
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
        configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true);
        configure(JsonParser.Feature.ALLOW_MISSING_VALUES, true);

        setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }

    public JsonObjectMapper() {
        this((JsonFactory) null);
    }

    private JsonObjectMapper(PrettyPrinter prettyPrinter) {
        this((JsonFactory) null);
        super.setDefaultPrettyPrinter(prettyPrinter);
    }

    public static JsonObjectMapper jsonObjectMapper() {
        return new JsonObjectMapper();
    }

    public static JsonObjectMapper jsonObjectMapper(TimeZone timeZone, PrettyPrinter prettyPrinter) {
        JsonObjectMapper objectMapper = new JsonObjectMapper(prettyPrinter);
        objectMapper.setTimeZone(timeZone);
        return objectMapper;
    }
}
