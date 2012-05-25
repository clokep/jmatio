package com.jmatio.types;

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
    }
    /**
     * Create a <code>{@link MLLogical}</code> array with given name,
     * and dimensions.
     *
     * @param name array name
     * @param dims array dimensions
     */
    public MLLogical(String name, int[] dims) {
        super(name, dims, MLArray.mxUINT8_CLASS, MLArray.mtFLAG_LOGICAL);
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

	public void dispose() {
		this.bools = null;
	}
}
