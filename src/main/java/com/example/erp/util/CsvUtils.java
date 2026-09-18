package com.example.erp.util;

import com.example.erp.exception.AppException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Shared CSV-parsing primitives for the bulk-import endpoints
// (ProductServiceImpl/CustomerServiceImpl/SupplierServiceImpl) — column
// access is by header name (via CSVRecord.isMapped/get), not position, so a
// CSV only needs to include the columns it actually has values for.
public final class CsvUtils {

    private CsvUtils() {
    }

    public static List<CSVRecord> parse(MultipartFile file) {
        try {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build();
            try (CSVParser parser = CSVParser.parse(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8), format)) {
                return parser.getRecords();
            }
        } catch (IOException e) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Could not read CSV file: " + e.getMessage());
        }
    }

    public static String getOptional(CSVRecord record, String column) {
        if (!record.isMapped(column)) return null;
        String value = record.get(column);
        return value == null || value.isBlank() ? null : value.trim();
    }

    public static String getRequired(CSVRecord record, String column) {
        String value = getOptional(record, column);
        if (value == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, column + " is required");
        }
        return value;
    }

    public static BigDecimal getDecimal(CSVRecord record, String column, BigDecimal defaultValue) {
        String value = getOptional(record, column);
        if (value == null) return defaultValue;
        return parseDecimal(column, value);
    }

    public static BigDecimal getRequiredDecimal(CSVRecord record, String column) {
        return parseDecimal(column, getRequired(record, column));
    }

    private static BigDecimal parseDecimal(String column, String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new AppException(HttpStatus.BAD_REQUEST, column + " is not a valid number: " + value);
        }
    }
}
