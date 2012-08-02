package com.jmatio.types;

import com.jmatio.common.MatLevel5DataTypes;

/**
 * Shorthand for an MLDouble empty array.
 */
public class MLEmptyArray extends MLDouble {
    public MLEmptyArray() {
        this(null);
    }

    public MLEmptyArray(String name) {
        super(name, new int[]{0, 0});
    }
}
