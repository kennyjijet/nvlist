package nl.weeaboo.vn;

import nl.weeaboo.common.Checks;

public final class StoragePrimitive {

    private final Object value;

    private StoragePrimitive(Object value) {
        this.value = Checks.checkNotNull(value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StoragePrimitive)) {
            return false;
        }
        StoragePrimitive p = (StoragePrimitive)obj;
        return value.equals(p.value);
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
        if (value == null) {
            return null;
        }
        return new StoragePrimitive(value);
    }

    public String toString(String defaultValue) {
        if (value != null) {
            return value.toString();
        }
        return defaultValue;
    }

}
