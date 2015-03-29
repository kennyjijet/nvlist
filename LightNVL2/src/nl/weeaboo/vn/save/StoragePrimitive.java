package nl.weeaboo.vn.save;

import nl.weeaboo.string.StringUtil2;

public final class StoragePrimitive {

    private final Object value;

    private StoragePrimitive(Object value) {
        this.value = value;
    }

    /**
     * @return The JSON value converted to a {@link StoragePrimitive} object, or {@code null} if the JSON
     *         value couldn't be parsed.
     */
    public static StoragePrimitive fromJson(String json) {
        // null
        if (json.equals("null")) {
            return new StoragePrimitive(null);
        }

        // boolean
        if (json.equals("true")) {
            return new StoragePrimitive(Boolean.TRUE);
        } else if (json.equals("false")) {
            return new StoragePrimitive(Boolean.FALSE);
        }

        // number
        try {
            return new StoragePrimitive(Double.parseDouble(json));
        } catch (NumberFormatException nfe) {
            // Not a number
        }

        // string
        if (json.startsWith("\"") && json.endsWith("\"")) {
            String str = StringUtil2.unescapeString(json.substring(1, json.length()-1));
            return new StoragePrimitive(str);
        }

        return null;
    }

    public String toJson() {
        if (value instanceof String) {
            // string
            return "\"" + StringUtil2.escapeString(value.toString()) + "\"";
        } else {
            // boolean, number, null
            return String.valueOf(value);
        }
    }

    @Override
    public int hashCode() {
        return (value != null ? value.hashCode() : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StoragePrimitive)) {
            return false;
        }
        StoragePrimitive p = (StoragePrimitive)obj;
        return value == p.value || (value != null && value.equals(p.value));
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static StoragePrimitive fromBoolean(boolean value) {
        return new StoragePrimitive(value);
    }

    public boolean toBoolean(boolean defaultValue) {
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        } else if (value instanceof String) {
            return ((String) value).equalsIgnoreCase("true");
        }
        return defaultValue;
    }

    public static StoragePrimitive fromDouble(double value) {
        return new StoragePrimitive(value);
    }

    public double toDouble(double defaultValue) {
        if (value instanceof Number) {
            Number number = (Number) value;
            return number.doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble(String.valueOf(value));
            } catch (NumberFormatException nfe) {
                // Value is not convertible to double
            }
        }
        return defaultValue;
    }

    public static StoragePrimitive fromString(String value) {
        return new StoragePrimitive(value);
    }

    public String toString(String defaultValue) {
        if (value != null) {
            return value.toString();
        }
        return defaultValue;
    }

}
