package com.jmatio.types;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.jmatio.io.OSMatTag;
import com.jmatio.common.MatDataTypes;
import com.jmatio.types.MLEmptyArray;

/**
 * Class represents opaque array (matrix).
 *
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLOpaque extends MLArray {
    private MLArray data;
    private String className;
    private String classType;

    public MLOpaque(String name, String classType, String className) {
        super(name, new int[]{1}, MLArray.mxOPAQUE_CLASS, 0);

        this.name = name;
        this.className = className;
        this.classType = classType;
    }

    public void set(MLArray value) {
        value.isChild = true;
        this.data = value;
    }

    public MLArray get() {
        return data;
    }

    public String contentToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.name + " (" + this.className + ") = \n");
        sb.append("\t");
        sb.append(this.get());
        sb.append("\n");

        return sb.toString();
    }

    public void dispose() {
        if (this.data != null)
            this.data = null;
    }

    public void writeData(DataOutputStream dos) throws IOException {
        this.get().writeMatrix(dos);
    }
}
