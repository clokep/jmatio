package com.jmatio.types;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Arrays;

import com.jmatio.common.MatLevel5DataTypes;
import com.jmatio.io.OSMatTag;

public class MLSparse extends MLNumericArray<Double> {
    int nzmax;
    private SortedSet<IndexMN> indexSet;
    // XXX Sparse can have other data types according to the spec, but Matlab
    // doesn't allow it.
    private SortedMap<IndexMN, Double> real;
    private SortedMap<IndexMN, Double> imaginary;

    /**
     * @param name
     * @param dims
     * @param attributes
     * @param nzmax
     */
    public MLSparse(String name, int[] dims, int attributes, int nzmax) {
        super(name, dims, MatLevel5DataTypes.mxSPARSE_CLASS, attributes);
        this.nzmax = nzmax;
    }

    protected void allocate() {
        this.real = new TreeMap<IndexMN, Double>();
        if (this.isComplex())
            this.imaginary = new TreeMap<IndexMN, Double>();
        this.indexSet = new TreeSet<IndexMN>();
    }

    /**
     * Gets maximum number of non-zero values
     *
     * @return maximum number of non-zero values
     */
    public int getMaxNZ() {
        return this.nzmax;
    }
    /**
     * Gets row indices
     *
     * <tt>ir</tt> points to an integer array of length nzmax containing the row indices of
     * the corresponding elements in <tt>pr</tt> and <tt>pi</tt>.
     */
    public int[] getIR() {
        int[] ir = new int[this.nzmax];
        int i = 0;
        for (IndexMN index : this.indexSet)
            ir[i++] = index.m;
        return ir;
    }

    /**
     * Gets column indices.
     *
     * <tt>jc</tt> points to an integer array of length N+1 that contains column index information.
     * For j, in the range <tt>0&lt;=j&lt;=N�1</tt>, <tt>jc[j]</tt> is the index in ir and <tt>pr</tt> (and <tt>pi</tt>
     * if it exists) of the first nonzero entry in the jth column and <tt>jc[j+1]�1</tt> index
     * of the last nonzero entry. As a result, <tt>jc[N]</tt> is also equal to nnz, the number
     * of nonzero entries in the matrix. If nnz is less than nzmax, then more nonzero
     * entries can be inserted in the array without allocating additional storage
     *
     * @return column indices
     */
    public int[] getJC() {
        int[] jc = new int[this.getN() + 1];
        // jc[j] is the number of nonzero elements in all preceeding columns
        for (IndexMN index : this.indexSet) {
            for (int column = index.n + 1; column < jc.length; ++column)
                ++jc[column];
        }
        return jc;
    }

    /* (non-Javadoc)
     * @see com.paradigmdesigner.matlab.types.GenericArrayCreator#createArray(int, int)
     */
    public Double[] createArray(int m, int n) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.paradigmdesigner.matlab.types.MLNumericArray#getReal(int, int)
     */
    public Double getReal(int m, int n) {
        IndexMN i = new IndexMN(m,n);
        if (this.real.containsKey(i))
            return this.real.get(i);
        return new Double(0);
    }

    /* (non-Javadoc)
     * @see com.jmatio.types.MLNumericArray#getReal(int)
     */
    public Double getReal(int index) {
        throw new IllegalArgumentException("Can't get Sparse array elements by index. " +
        "Please use getReal(int index) instead.");
    }

    /**
     * @param value
     * @param m
     * @param n
     */
    public void setReal(Double value, int m, int n) {
        IndexMN i = new IndexMN(m,n);
        this.indexSet.add(i);
        this.real.put(i, value);
    }

    /**
     * @param value
     * @param index
     */
    public void setReal(Double value, int index) {
        throw new IllegalArgumentException("Can't set Sparse array elements by index. " +
                "Please use setReal(Double value, int m, int n) instead.");
    }

    /**
     * @param value
     * @param m
     * @param n
     */
    public void setImaginary(Double value, int m, int n) {
        IndexMN i = new IndexMN(m,n);
        this.indexSet.add(i);
        this.imaginary.put(i, value);
    }

    /**
     * @param value
     * @param index
     */
    public void setImaginary(Double value, int index) {
        throw new IllegalArgumentException("Can't set Sparse array elements by index. " +
        "Please use setImaginary(Double value, int m, int n) instead.");
    }

    /* (non-Javadoc)
     * @see com.paradigmdesigner.matlab.types.MLNumericArray#getImaginary(int, int)
     */
    public Double getImaginary(int m, int n) {
        IndexMN i = new IndexMN(m,n);
        if (this.imaginary.containsKey(i))
            return this.imaginary.get(i);
        return new Double(0);
    }

    /* (non-Javadoc)
     * @see com.jmatio.types.MLNumericArray#getImaginary(int)
     */
    public Double getImaginary(int index) {
        throw new IllegalArgumentException("Can't get Sparse array elements by index. " +
        "Please use getImaginary(int index) instead.");
    }

    /**
     * Returns the real part (PR) array. PR has length number-of-nonzero-values.
     *
     * @return real part
     */
    public Double[] exportReal() {
        Double[] ad = new Double[this.indexSet.size()];
        int i = 0;
        for (IndexMN index : this.indexSet) {
            if (this.real.containsKey(index))
                ad[i] = real.get(index);
            else
                ad[i] = 0.0;
            ++i;
        }
        return ad;
    }

    /**
     * Returns the imaginary part (PI) array. PI has length number-of-nonzero-values.
     *
     * @return imaginary array
     */
    public Double[] exportImaginary() {
        Double[] ad = new Double[this.indexSet.size()];
        int i = 0;
        for (IndexMN index: this.indexSet) {
            if (this.imaginary.containsKey(index))
                ad[i] = imaginary.get(index);
            else
                ad[i] = 0.0;
            i++;
        }
        return ad;
    }

    /* (non-Javadoc)
     * @see com.paradigmdesigner.matlab.types.MLArray#contentToString()
     */
    public String contentToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.name + " = \n");

        for (IndexMN i : this.indexSet) {
            sb.append("\t(");
            sb.append(i.m + "," + i.n);
            sb.append(")");
            sb.append("\t" + this.getReal(i.m, i.n));
            if (this.isComplex())
                sb.append("+" + this.getImaginary(i.m, i.n));
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Matrix index (m,n)
     *
     * @author Wojciech Gradkowski <wgradkowski@gmail.com>
     */
    private class IndexMN implements Comparable<IndexMN> {
        int m;
        int n;

        public IndexMN(int m, int n) {
            this.m = m;
            this.n = n;
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(IndexMN anOtherIndex) {
            return getIndex(m, n) - getIndex(anOtherIndex.m, anOtherIndex.n);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object o) {
            if (o instanceof IndexMN)
                return this.m == ((IndexMN)o).m && this.n == ((IndexMN)o).n;
            return super.equals(o);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("{");
            sb.append("m=" + m);
            sb.append(", ");
            sb.append("n=" + n);
            sb.append("}");
            return sb.toString();
        }
    }

    public Double buildFromBytes(byte[] bytes) {
        if (bytes.length != this.getBytesAllocated()) {
            throw new IllegalArgumentException(
                        "To build from byte array I need array of size: "
                                + this.getBytesAllocated() );
        }
        return ByteBuffer.wrap(bytes).getDouble();
    }

    public int getBytesAllocated() {
        return Double.SIZE >> 3;
    }

    public Class<Double> getStorageClazz() {
        return Double.class;
    }

    public byte[] getByteArray(Double value) {
        ByteBuffer buff = ByteBuffer.allocate(this.getBytesAllocated());
        buff.putDouble(value);
        return buff.array();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof  MLSparse) {
            // Check if the indices and real values are equal.
            boolean result = Arrays.equals(this.getIR(), ((MLSparse)o).getIR()) &&
                             Arrays.equals(this.getJC(), ((MLSparse)o).getJC()) &&
                             Arrays.equals(this.exportReal(), ((MLSparse)o).exportReal());

            if (this.isComplex() && result)
                result &= Arrays.equals(this.exportImaginary(), ((MLSparse)o).exportImaginary());
            return result;
        }
        return super.equals(o);
    }

    public void writeData(DataOutputStream dos) throws IOException {
        int[] ai;

        // Write ir.
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream bufferDOS = new DataOutputStream(buffer);
        ai = this.getIR();
        for (int i : ai)
            bufferDOS.writeInt(i);
        OSMatTag tag = new OSMatTag(MatLevel5DataTypes.miINT32, buffer.toByteArray() );
        tag.writeTo(dos);

        // Write jc.
        buffer = new ByteArrayOutputStream();
        bufferDOS = new DataOutputStream(buffer);
        ai = this.getJC();
        for (int i : ai)
            bufferDOS.writeInt(i);
        tag = new OSMatTag(MatLevel5DataTypes.miINT32, buffer.toByteArray());
        tag.writeTo(dos);

        // Write real part.
        buffer = new ByteArrayOutputStream();
        bufferDOS = new DataOutputStream(buffer);
        Double[] ad = this.exportReal();
        for (int i = 0; i < ad.length; ++i)
            bufferDOS.writeDouble(ad[i].doubleValue());
        tag = new OSMatTag(MatLevel5DataTypes.miDOUBLE, buffer.toByteArray());
        tag.writeTo(dos);

        // Write imaginary part.
        if (this.isComplex()) {
            buffer = new ByteArrayOutputStream();
            bufferDOS = new DataOutputStream(buffer);
            ad = this.exportImaginary();
            for (int i = 0; i < ad.length; ++i)
                bufferDOS.writeDouble(ad[i].doubleValue());
            tag = new OSMatTag(MatLevel5DataTypes.miDOUBLE, buffer.toByteArray() );
            tag.writeTo(dos);
        }
    }
}
