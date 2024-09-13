package expression.impl;

import expression.api.Data;
import expression.api.DataType;

import java.io.Serializable;

public class DataImpl implements Data, Serializable {

    public static final String undefiled = "!UNDEIFINED!";
    public static final String BoolUndefiled = "UNKNOWN";
    public static final String empty = "";

    private DataType type;
    private Object value;

    public DataImpl() {}

    public DataImpl(DataType type, Object value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public DataType getType() {
        return type;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (type == DataType.NUMERIC) {
            double valueDouble = (double) value;
            int valueInt = (int) valueDouble;
            if (valueInt == valueDouble) {
                return Integer.toString(valueInt);
            }
            else{
                return String.format("%.2f", valueDouble);
            }
        }

        return value.toString();
    }
}
