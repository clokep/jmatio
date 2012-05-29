package com.jmatio.types;

public class MLEmptyArray extends MLDouble {
    public MLEmptyArray() {
        this(null);
    }

    public MLEmptyArray(String name) {
        this(name, new int[] {0, 0}, MLArray.mxDOUBLE_CLASS, 0);
    }

    public MLEmptyArray(String name, int type) {
        this(name, new int[] {0, 0}, type, 0);
    }

    public MLEmptyArray(String name, int[] dims, int type) {
        this(name, dims, type, 0);
    }

    public MLEmptyArray(String name, int[] dims, int type, int attributes) {
        super(name, dims, type, attributes);
    }
}
