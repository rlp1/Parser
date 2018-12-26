package rlp.parser;

import java.util.List;
import java.util.Map;

/**
 * @author rlp
 * @since 1.0
 */
public class ComplexObject {

    private final Object intrinsicValue;

    private ComplexObject(final Object value) {
        this.intrinsicValue = value;
    } // package-private

    public byte byteValue() { return (byte) this.intrinsicValue; }

    public short shortValue() { return (short) this.intrinsicValue; }

    public int intValue() { return (int) this.intrinsicValue; }

    public float floatValue() { return (float) this.intrinsicValue; }

    public long longValue() { return (long) this.intrinsicValue; }

    public double doubleValue() { return (double) this.intrinsicValue; }

    public String stringValue() { return (String) this.intrinsicValue; }

    public List<?> listValue() { return (List<?>) this.intrinsicValue; }

    public Map<?, ?> mapValue() { return (Map<?, ?>) this.intrinsicValue; }

    @Override
    public String toString() {
        return "" + this.intrinsicValue;
    }

    public static ComplexObject newComplexObject(final Object value) {
        return new ComplexObject(value);
    }
}
