package com.satyanand.shardedsagawallet.services.saga;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class SagaContext {

    Map<String, Object> data;

    public SagaContext(Map<String, Object> data) {
        this.data = data != null ? data : new HashMap<>();
    }

    public void put(String key, Object value){
        data.put(key, value);
    }

    public Object get(String key){
        return data.get(key);
    }

    public Long getLong(String key){
        Object value = get(key);

        if( value == null ) return null;

        if(value instanceof Number){
            return ((Number) value).longValue();
        }

        if(value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                        "Value for key '" + key + "' is not a valid Long: " + value
                );
            }
        }

        throw new IllegalArgumentException(
                "Value for key '" + key + "' cannot be converted to Long: " + value.getClass()
        );
    }

    public BigDecimal getBigDecimal(String key) {
        Object value = get(key);

        if (value == null) return null;

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        if (value instanceof Integer || value instanceof Long) {
            return BigDecimal.valueOf(((Number) value).longValue());
        }

        if (value instanceof Float || value instanceof Double) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }

        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid BigDecimal: " + value);
            }
        }

        throw new IllegalArgumentException("Unsupported type for BigDecimal: " + value.getClass());
    }
}
