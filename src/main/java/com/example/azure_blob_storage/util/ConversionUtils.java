package com.example.azure_blob_storage.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class ConversionUtils {

    private final ObjectMapper jsonMapper;

    public ConversionUtils(@Nullable ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper == null ? newObjectMapper() : jsonMapper;
    }

    /**
     * newObjectMapper.
     *
     * @return ObjectMapper
     */
    public ObjectMapper newObjectMapper() {
        ObjectMapper objectMapper = JsonMapper.builder().addModule(new Jdk8Module()).addModule(new JavaTimeModule())
                .build();
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, Boolean.FALSE);
        objectMapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, Boolean.FALSE);
        objectMapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, Boolean.FALSE);
        return objectMapper;
    }

    /**
     * toJson.
     *
     * @param obj obj
     * @return String
     */
    public String toJson(Object obj) {
        try {
            return jsonMapper.writeValueAsString(obj);
        } catch (IOException e) {
            throw new IllegalStateException("Error formatting execution properties: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fromJson(String json) {
        return fromJson(json, Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fromJson(byte[] json) throws IOException {
        return fromJson(json, Map.class);
    }

    /**
     * fromJson.
     *
     * @param json json
     * @param cls  cls
     * @param      <T> type
     *
     */
    public <T> T fromJson(String json, Class<T> cls) {
        try {
            return jsonMapper.readValue(json, cls);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error parsing execution properties: " + e.getMessage(), e);
        }
    }

    public <T> T fromJson(byte[] json, Class<T> cls) throws IOException {
        return jsonMapper.readValue(json, cls);
    }

    public byte[] toZip(String value) {
        return toZip(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * toZip.
     *
     * @param bytes bytes
     * @return byte array
     */
    public byte[] toZip(byte[] bytes) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                zos.putNextEntry(new ZipEntry("utf8.txt"));
                zos.write(bytes);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public byte[] toGzip(byte[] bytes) {
        return doGzip(bytes);
    }

    private byte[] doGzip(byte[] bytes) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (GZIPOutputStream zos = new GZIPOutputStream(baos)) {
                zos.write(bytes);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * toString.
     *
     * @param value value
     * @return String
     */
    public String toString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * toBoolean.
     *
     * @param value value
     * @return Boolean
     */
    public Boolean toBoolean(Object value) {
        if (value == null) {
            return Boolean.FALSE;
        }
        return Boolean.valueOf(value.toString());
    }

    /**
     * toDouble.
     *
     * @param value value
     * @return Double
     */
    public Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        return Double.valueOf(value.toString());
    }

    /**
     * toLocalDate.
     *
     * @param value value
     * @return LocalDate
     */
    public LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        return LocalDate.parse(value.toString());
    }

    /**
     * toLocalDateTime.
     *
     * @param value value
     * @return LocalDateTime
     */
    public LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        return LocalDateTime.parse(value.toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
