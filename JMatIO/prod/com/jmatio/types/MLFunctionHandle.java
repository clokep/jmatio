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
 * Class represents function_handle array (matrix).
 *
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLFunctionHandle extends MLArray {
    private String functionHandle;
    /** Known types: simple, anonymous. */
    private String type;

    public MLFunctionHandle(String name, int[] dims) {
        super(name, dims, MLArray.mxFUNCTION_CLASS, 0);
    }

    public MLFunctionHandle(String name, int[] dims, int type, int attributes) {
        super(name, dims, type, attributes);
    }

    public void set(MLStructure data) {
        //this.data = data;
    }

    /*public String get() {
        //return
    }

    public MLStructure getFunctionl() {
        //return this.data.getField("function");
    }*/

    public void dispose() {
        //if (this.data != null)
            //this.data = null;
    }

    public String contentToString() {
        return null;
    }

    public void writeData(DataOutputStream dos) throws IOException {
        //this.writeMatrix(dos);
    }
}
