package com.yourcompany.reception.util;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
public final class Json {
    private static final ObjectMapper MAPPER=new ObjectMapper();
    private Json(){}
    public static String encode(Object value) {
        try { return MAPPER.writeValueAsString(value); } catch(JsonProcessingException ex) { throw new IllegalStateException("JSON serialization failed",ex); }
    }
}
