package com.jmatio.types;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.jmatio.io.OSMatTag;
import com.jmatio.common.MatDataTypes;

/**
 * Class represents Logical (boolean) array (matrix).
 *
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLLogical extends MLArray {
    Boolean[] bools;

    /**
     * Normally this constructor is used only by MatFileReader and MatFileWriter
     *
     * @param name array name
     * @param dims array dimensions
     * @param type array type: here <code>mxUINT8_CLASS</code>
     * @param attributes array flags: here <code>mtFLAG_LOGICAL</code>
     */
    public MLLogical(String name, int[] dims, int type, int attributes) {
        super(name, dims, type, attributes);
        this.allocate();
    }
    /**
     * Create a <code>{@link MLLogical}</code> array with given name,
     * and dimensions.
     *
     * @param name array name
     * @param dims array dimensions
     */
    public MLLogical(String name, int[] dims) {
        this(name, dims, MLArray.mxUINT8_CLASS, MLArray.mtFLAG_LOGICAL);
    }

    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style:
     * construct a 2D real matrix from a one-dimensional packed array
     *
     * @param name array name
     * @param vals One-dimensional array of Boolean, packed by columns (ala Fortran).
     * @param m Number of rows
     */
    public MLLogical(String name, Boolean[] vals, int m) {
        this(name, new int[] {m, vals.length / m}, MLArray.mxUINT8_CLASS, MLArray.mtFLAG_LOGICAL);
        if ((vals.length % m) != 0) {
            throw new IllegalArgumentException("The number of values provided (" +
                                               vals.length +
                                               ") is not equally divisible by the number of rows ("
                                               + m + ").");
        }

        this.set(vals);
    }

    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style:
     * construct a matrix from a one-dimensional packed array
     *
     * @param name array name
     * @param vals One-dimensional array of boolean, packed by columns (ala Fortran).
     * @param m Number of rows
     */
    public MLLogical(String name, boolean[] vals, int m) {
        this(name, MLLogical.castToObject(vals), m);
    }

    /**
     * Normally this constructor is used only by MatFileReader and MatFileWriter
     *
     * @param array A numeric array that is actually logical.
     */
    public MLLogical(MLNumericArray<?> array) {
        this(array.name, array.dims, array.type, array.attributes);

        for (int i = 0; i < this.getSize(); ++i)
            this.set((Number)array.get(i), i);
    }

    protected void allocate() {
        this.bools = new Boolean[this.getSize()];
    }

    /**
     * @param vector
     */
    public void set(boolean[] vector) {
        this.set(MLLogical.castToObject(vector));
    }

    public void set(Boolean[] vector) {
        // Fill the array.
        for (int i = 0; i < vector.length; ++i)
            this.set(vector[i], i);
    }

    public void set(Boolean value, int index) {
        this.bools[index] = value;
    }

    public void set(Number value, int index) {
        this.bools[index] = value.longValue() != 0 || value.doubleValue() != 0;
    }

    public Boolean get(int m, int n) {
        return this.get(this.getIndex(m, n));
    }

    public Boolean get(int index) {
        return this.bools[index];
    }

    public boolean[] get() {
        return MLLogical.castToPrimitive(this.bools);
    }

    /**
     * Casts <code>boolean[]</code> to <code>Boolean[]</code>.
     *
     * @param d <code>boolean[]</code>
     * @return result <code>Boolean[]</code>
     */
    protected static Boolean[] castToObject(boolean[] d) {
        Boolean[] dest = new Boolean[d.length];
        for (int i = 0; i < d.length; ++i)
            dest[i] = d[i];
        return dest;
    }

    /**
     * Casts <code>Boolean[]</code> to <code>boolean[]</code>.
     *
     * @param d <code>Boolean[]</code>
     * @return result <code>boolean[]</code>
     */
    protected static boolean[] castToPrimitive(Boolean[] d) {
        boolean[] dest = new boolean[d.length];
        for (int i = 0; i < d.length; ++i)
            dest[i] = d[i].booleanValue();
        return dest;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MLLogical)
            return Arrays.equals(bools, ((MLLogical)o).bools);
        return super.equals(o);
    }

    public void dispose() {
        this.bools = null;
    }

    public String contentToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.name + " = \n");

        if (this.getSize() > 1000) {
            sb.append("Cannot display variables with more than 1000 elements.");
            return sb.toString();
        }
        for (int m = 0; m < this.getM(); ++m) {
           sb.append("\t");
           for (int n = 0; n < this.getN(); ++n) {
               sb.append(this.get(m, n));
               sb.append("\t");
           }
           sb.append("\n");
        }
        return sb.toString();
    }

    public void writeData(DataOutputStream dos) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream bufferDOS = new DataOutputStream(buffer);
        for (int i = 0; i < this.bools.length; ++i)
            bufferDOS.writeByte(this.bools[i] ? 1 : 0);

        OSMatTag tag = new OSMatTag(MatDataTypes.miUINT8, buffer.toByteArray());
        tag.writeTo(dos);
    }
}
