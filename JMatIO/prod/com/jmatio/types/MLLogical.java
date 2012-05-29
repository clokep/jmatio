package com.jmatio.types;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.jmatio.io.OSArrayTag;
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
     * @param vals One-dimensional array of boolean, packed by columns (ala Fortran).
     * @param m Number of rows
     */
    public MLLogical(String name, boolean[] vals, int m) {
        this(name, new int[] {m, vals.length/m}, MLArray.mxUINT8_CLASS, MLArray.mtFLAG_LOGICAL);
        this.set(vals);
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

    /**
     * Casts <code>Double[]</code> to <code>byte[]</code>
     *
     * @param d <code>Long[]</code>
     * @return result <code>long[]</code>
     */
    protected static Boolean[] castToObject(boolean[] d) {
    	Boolean[] dest = new Boolean[d.length];
        for (int i = 0; i < d.length; ++i)
            dest[i] = d[i];
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

    public void writeData(DataOutputStream dos) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream bufferDOS = new DataOutputStream(buffer);
        for (int i = 0; i < this.bools.length; ++i)
            bufferDOS.writeByte(this.bools[i] ? 1 : 0);

        OSArrayTag tag = new OSArrayTag(MatDataTypes.miUINT8, buffer.toByteArray());
        tag.writeTo(dos);
    }
}
