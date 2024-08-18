package expression.impl;

import expression.api.Data;
import expression.api.DataType;

public class DataImpl implements Data {

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
}