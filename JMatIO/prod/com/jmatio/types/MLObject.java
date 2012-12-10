package com.jmatio.types;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.jmatio.common.MatLevel5DataTypes;
import com.jmatio.io.OSMatTag;

/**
 * This class represents a Matlab object (structure array).
 *
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLObject extends MLStructure {
    /**
     * The class name of object.
     */
    private String className;

    public MLObject(String name, int[] dims) {
        this(name, dims, false);
    }

    public MLObject(String name, int[] dims, boolean global) {
        super(name, dims, global);
        this.type = MatLevel5DataTypes.mxOBJECT_CLASS;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Gets the class name as an array of bytes.
     *
     * @return class name as bytes
     */
    public byte[] getClassNameToByteArray() {
        return this.className.getBytes();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MLObject) {
            // Ensure the class names are equal.
            if (!this.className.equals(((MLObject)o).className))
                return false;
            // Fall through to super and check that the fields are equal.
        }
        return super.equals(o);
    }

    /* (non-Javadoc)
     * @see com.paradigmdesigner.matlab.types.MLArray#contentToString()
     */
    public String contentToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.name + " (" + this.className + ") = \n");

        if (this.getSize() == 1) {
            for (String key : this.keys)
                sb.append("\t" + key + " : " + this.getField(key) + "\n");
        } else {
            sb.append("\n");
            sb.append(this.getM() + "x" + this.getN());
            sb.append(" object with fields: \n");
            for (String key : this.keys)
                sb.append("\t" + key + "\n");
        }
        return sb.toString();
    }

    public void writeData(DataOutputStream dos) throws IOException {
        // Write the class name.
        this.writeString(dos, this.className);

        // Write the structure representation.
        super.writeData(dos);
    }
}
