package com.jmatio.types;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.jmatio.common.MatLevel5DataTypes;
import com.jmatio.io.OSMatTag;

/**
 * Abstract class for numeric arrays.
 *
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 *
 * @param <T>
 */
public abstract class MLNumericArray<T extends Number> extends MLArray
                                                       implements GenericArrayCreator<T>,
                                                                  ByteStorageSupport<T> {
    private ByteBuffer real;
    private ByteBuffer imaginary;
    /** The buffer for creating Number from bytes */
    private byte[] bytes;

    /**
     * Normally this constructor is used only by MatFileReader and MatFileWriter
     *
     * @param name array name
     * @param dims array dimensions
     * @param type array type
     * @param attributes array flags
     */
    public MLNumericArray(String name, int[] dims, int type, int attributes) {
        super(name, dims, type, attributes);
        this.allocate();
    }

    protected void allocate() {
        this.real = ByteBuffer.allocate(this.getSize() * this.getBytesAllocated());
        if (this.isComplex())
            this.imaginary = ByteBuffer.allocate(this.getSize() * this.getBytesAllocated());
        this.bytes = new byte[this.getBytesAllocated()];
    }

    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style:
     * construct a 2D real matrix from a one-dimensional packed array
     *
     * @param name array name
     * @param type array type
     * @param vals One-dimensional array of T, packed by columns (ala Fortran).
     * @param m Number of rows
     */
    public MLNumericArray(String name, int type, T[] vals, int m) {
        this(name, new int[] {m, vals.length / m}, type, 0);
        if ((vals.length % m) != 0) {
            throw new IllegalArgumentException("The number of values provided (" +
                                               vals.length +
                                               ") is not equally divisible by the number of rows ("
                                               + m + ").");
        }

        this.setReal(vals);
    }

    /**
     * Gets single real array element of A(m,n).
     *
     * @param m row index
     * @param n column index
     * @return array element
     */
    public T getReal(int m, int n) {
        return this.getReal(this.getIndex(m, n));
    }

    /**
     * @param index
     * @return array element
     */
    public T getReal(int index) {
        return this._get(this.real, index);
    }

    /**
     * Sets single real array element.
     *
     * @param value element value
     * @param m row index
     * @param n column index
     */
    public void setReal(T value, int m, int n) {
        this.setReal(value, this.getIndex(m, n));
    }

    /**
     * Sets single real array element.
     *
     * @param value element value
     * @param index column-packed vector index
     */
    public void setReal(T value, int index) {
        this._set(this.real, value, index);
    }

    /**
     * Sets real part of matrix
     *
     * @param vector column-packed vector of elements
     */
    public void setReal(T[] vector) {
        if (vector.length != this.getSize())
            throw new IllegalArgumentException("Matrix dimensions do not match. " + this.getSize() + " not " + vector.length);
        // Fill the array
        for (int i = 0; i < vector.length; ++i)
            this.setReal(vector[i], i);
    }

    /**
     * Sets single imaginary array element.
     *
     * @param value element value
     * @param m row index
     * @param n column index
     */
    public void setImaginary(T value, int m, int n) {
        this.setImaginary(value, this.getIndex(m, n));
    }

    /**
     * Sets single real array element.
     *
     * @param value element value
     * @param index column-packed vector index
     */
    public void setImaginary(T value, int index) {
        if (!this.isComplex())
            throw new RuntimeException("Array is not complex");
        this._set(imaginary, value, index);
    }

    /**
     * Gets single imaginary array element of A(m,n).
     *
     * @param m row index
     * @param n column index
     * @return array element
     */
    public T getImaginary(int m, int n) {
        return this.getImaginary(this.getIndex(m, n));
    }

    /**
     * @param index
     * @return array element
     */
    public T getImaginary(int index) {
        if (!this.isComplex())
            throw new RuntimeException("Array is not complex");
        return this._get(this.imaginary, index);
    }

    /**
     * Does the same as <code>setReal</code>.
     *
     * @param value element value
     * @param m row index
     * @param n column index
     */
    public void set(T value, int m, int n) {
        if (this.isComplex())
            throw new IllegalStateException("Cannot use this method for Complex matrices");
        this.setReal(value, m, n);
    }
    /**
     * Does the same as <code>setReal</code>.
     *
     * @param value element value
     * @param index column-packed vector index
     */
    public void set(T value, int index) {
        if (this.isComplex())
            throw new IllegalStateException("Cannot use this method for Complex matrices");
        this.setReal(value, index);
    }
    /**
     * Does the same as <code>getReal</code>.
     *
     * @param m row index
     * @param n column index
     * @return array element
     */
    public T get(int m, int n) {
        if (this.isComplex())
            throw new IllegalStateException("Cannot use this method for Complex matrices");
        return this.getReal(m, n);
    }
    /**
     * @param index
     * @return array element
     */
    public T get(int index) {
        if (this.isComplex())
            throw new IllegalStateException("Cannot use this method for Complex matrices");
        return this._get(this.real, index);
    }
    /**
     * @param vector
     */
    public void set(T[] vector) {
        if (this.isComplex())
            throw new IllegalStateException("Cannot use this method for Complex matrices");
        this.setReal(vector);
    }

    private int getByteOffset(int index) {
        return index * this.getBytesAllocated();
    }

    protected T _get(ByteBuffer buffer, int index) {
        buffer.position(this.getByteOffset(index));
        buffer.get(this.bytes, 0, this.bytes.length);
        return this.buildFromBytes(this.bytes);
    }

    protected void _set(ByteBuffer buffer, T value, int index) {
        buffer.position(this.getByteOffset(index));
        buffer.put(this.getByteArray(value));
    }

    public void putImaginaryByteBuffer(ByteBuffer buff) {
        if (!this.isComplex())
            throw new RuntimeException("Array is not complex");
        this.imaginary.rewind();
        this.imaginary.put(buff);
    }

    public ByteBuffer getImaginaryByteBuffer() {
        return this.imaginary;
    }

    public void putRealByteBuffer(ByteBuffer buff) {
        this.real.rewind();
        this.real.put(buff);
    }

    public ByteBuffer getRealByteBuffer() {
        return this.real;
    }

    /* (non-Javadoc)
     * @see com.jmatio.types.MLArray#contentToString()
     */
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
               sb.append(this.getReal(m, n));
               if (this.isComplex())
                   sb.append("+" + this.getImaginary(m, n) + "i");
               sb.append("\t");
           }
           sb.append("\n");
        }
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof  MLNumericArray<?>) {
            boolean result = this.real.rewind().equals(((MLNumericArray<?>)o).real.rewind()) &&
                             Arrays.equals(this.dims, ((MLArray)o).dims);
            if (this.isComplex() && result)
                result &= this.imaginary.rewind().equals(((MLNumericArray<?>)o).imaginary.rewind());
            return result;
        }
        return super.equals(o);
    }

    public void dispose() {
        if (this.real != null)
            this.real.clear();
        if (this.imaginary != null)
            this.imaginary.clear();
    }

    public void writeData(DataOutputStream dos) throws IOException {
        int type;
        switch (this.type) {
            case MatLevel5DataTypes.mxDOUBLE_CLASS:
                type = MatLevel5DataTypes.miDOUBLE;
                break;
            case MatLevel5DataTypes.mxSINGLE_CLASS:
                type = MatLevel5DataTypes.miSINGLE;
                break;
            case MatLevel5DataTypes.mxINT8_CLASS:
                type = MatLevel5DataTypes.miINT8;
                break;
            case MatLevel5DataTypes.mxUINT8_CLASS:
                type = MatLevel5DataTypes.miUINT8;
                break;
            case MatLevel5DataTypes.mxINT16_CLASS:
                type = MatLevel5DataTypes.miINT16;
                break;
            case MatLevel5DataTypes.mxUINT16_CLASS:
                type = MatLevel5DataTypes.miUINT16;
                break;
            case MatLevel5DataTypes.mxINT32_CLASS:
                type = MatLevel5DataTypes.miINT32;
                break;
            case MatLevel5DataTypes.mxUINT32_CLASS:
                type = MatLevel5DataTypes.miUINT32;
                break;
            case MatLevel5DataTypes.mxINT64_CLASS:
                type = MatLevel5DataTypes.miINT64;
                break;
            case MatLevel5DataTypes.mxUINT64_CLASS:
                type = MatLevel5DataTypes.miUINT64;
                break;
            default:
                type = MatLevel5DataTypes.miUNKNOWN;
        }

        // Write real part.
        OSMatTag tag = new OSMatTag(type, this.getRealByteBuffer());
        tag.writeTo(dos);

        // Write imaginary part.
        if (this.isComplex()) {
            tag = new OSMatTag(type, this.getImaginaryByteBuffer());
            tag.writeTo(dos);
        }
    }
}
