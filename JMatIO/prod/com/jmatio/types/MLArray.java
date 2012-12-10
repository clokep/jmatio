package com.jmatio.types;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.jmatio.common.MatLevel5DataTypes;
import com.jmatio.common.VariableUtils;
import com.jmatio.io.OSMatTag;
import com.jmatio.io.MatlabIOException;

public abstract class MLArray {
    protected boolean global = false;
    protected boolean complex = false;
    protected boolean logical = false;

    /** The dimensions of the array. */
    protected int dims[];
    /** The array name. */
    public String name;
    /** The array type. */
    protected int type = MatLevel5DataTypes.mxUNKNOWN_CLASS;

    /** This is true for sub-elements of structs & cells where name does not matter. */
    public boolean isChild = false;

    public MLArray(String name, int[] dims, boolean global) {
        // MATLAB arrays must always have two dimensions. If only one is given, assume 2D with the other dimension equal to 1.
        if (dims.length == 1) {
            // Since we have a vector but don't know the real dimensions, just assume it's a column vector
            this.dims = new int[2];
            this.dims[0] = dims[0];
            this.dims[1] = 1;
        } else {
            this.dims = new int[dims.length];
            System.arraycopy(dims, 0, this.dims, 0, dims.length);
        }

        if (name != null && !name.equals(""))
            this.name = name;
        else
            this.name = "@"; //default name

        this.global = global;
    }

    /**
     * Gets the array name.
     *
     * @return array name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets whether the array is stored globally.
     *
     * @return whether the array is stored globally
     */
    public boolean isGlobal() {
        return this.global;
    }
    
    /**
     * Gets whether the elements are complex.
     *
     * @return whether the array is complex
     */
    public boolean isComplex() {
        return this.complex;
    }

    /**
     * Gets whether the elements are logical (boolean).
     *
     * @return whether the array is logical
     */
    public boolean isLogical() {
        return this.logical;
    }
    
    public int[] getDimensions() {
        int ai[] = null;
        if (dims != null) {
            ai = new int[this.dims.length];
            System.arraycopy(this.dims, 0, ai, 0, this.dims.length);
        }
        return ai;
    }

    /**
     * Get the number of rows.
     */
    public int getM() {
        int i = 0;
        if (this.dims != null)
            i = this.dims[0];
        return i;
    }

    /**
     * Get the number of columns and higher dimensions.
     */
    public int getN() {
        int i = 0;
        if (this.dims != null) {
            if (this.dims.length > 2) {
                i = 1;
                for(int j = 1; j < this.dims.length; j++)
                    i *= this.dims[j];
            } else
                i = this.dims[1];
        }
        return i;
    }

    /**
     * Get the number of dimensions.
     */
    public int getNDimensions() {
        int i = 0;
        if (this.dims != null)
            i = this.dims.length;
        return i;
    }
    /**
     * Get the total number of elements.
     */
    public int getSize() {
        return this.getM() * this.getN();
    }

    public int getType() {
        return this.type;
    }

    public boolean isEmpty() {
        return this.getN() == 0;
    }

    /*public boolean isCell() {
        return this.type == MatLevel5DataTypes.mxCELL_CLASS;
    }

    public boolean isChar() {
        return this.type == MatLevel5DataTypes.mxCHAR_CLASS;
    }*/

    public boolean isSparse() {
        //return this.type == MatLevel5DataTypes.mxSPARSE_CLASS;
        // XXX
        return false;
    }

    /*public boolean isStruct() {
        return this.type == MatLevel5DataTypes.mxSTRUCT_CLASS;
    }

    public boolean isDouble() {
        return this.type == MatLevel5DataTypes.mxDOUBLE_CLASS;
    }

    public boolean isSingle() {
        return this.type == MatLevel5DataTypes.mxSINGLE_CLASS;
    }

    public boolean isInt8() {
        return this.type == MatLevel5DataTypes.mxINT8_CLASS;
    }

    public boolean isUint8() {
        return this.type == MatLevel5DataTypes.mxUINT8_CLASS;
    }

    public boolean isInt16() {
        return this.type == MatLevel5DataTypes.mxINT16_CLASS;
    }

    public boolean isUint16() {
        return this.type == MatLevel5DataTypes.mxUINT16_CLASS;
    }

    public boolean isInt32() {
        return this.type == MatLevel5DataTypes.mxINT32_CLASS;
    }

    public boolean isUint32() {
        return this.type == MatLevel5DataTypes.mxUINT32_CLASS;
    }

    public boolean isInt64(){
        return this.type == MatLevel5DataTypes.mxINT64_CLASS;
    }

    public boolean isUint64() {
        return this.type == MatLevel5DataTypes.mxUINT64_CLASS;
    }

    public boolean isObject() {
        return this.type == MatLevel5DataTypes.mxOBJECT_CLASS;
    }

    public boolean isOpaque() {
        return this.type == MatLevel5DataTypes.mxOPAQUE_CLASS;
    }

    public boolean isFunctionObject() {
        return this.type == MatLevel5DataTypes.mxFUNCTION_CLASS;
    }

    public boolean isUnknown() {
        return this.type == MatLevel5DataTypes.mxUNKNOWN_CLASS;
    }*/

    protected int getIndex(int m, int n) {
        return m + n * this.getM();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (this.dims != null) {
            sb.append('[');
            // Print out the size: nD if more than three dimensions or nxm(xo) if <= 3 dimensions.
            if (this.dims.length > 3) {
                sb.append(this.dims.length);
                sb.append('D');
            } else {
                sb.append(this.dims[0]);
                sb.append('x');
                sb.append(this.dims[1]);
                if (this.dims.length == 3) {
                    sb.append('x');
                    sb.append(this.dims[2]);
                }
            }
            sb.append("  ");
            sb.append(this.type);
            sb.append(" array");
            if (this.isSparse()) {
                sb.append(" (sparse");
                if (this.isComplex())
                    sb.append(" complex");
                sb.append(")");
            } else if (this.isComplex())
                sb.append(" (complex)");
            sb.append(']');
        } else
            sb.append("[invalid]");
        return sb.toString();
    }

    abstract public void dispose();

    /**
     * Writes MATRIX into <code>OutputStream</code>.
     *
     * @param os <code>OutputStream</code>
     * @throws IOException
     */
    public void writeMatrix(OutputStream os) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // Flags.
        this.writeFlags(dos);

        // Dimensions.
        this.writeDimensions(dos);

        // Array name.
        this.writeName(dos);

        // Write the actual data.
        this.writeData(dos);

        // Write matrix tag.
        OSMatTag tag = new OSMatTag(MatLevel5DataTypes.miMATRIX, baos.toByteArray());
        tag.writeTo(os);
    }

    /**
     * Writes MATRIX flags into <code>OutputStream</code>.
     *
     * @param os <code>OutputStream</code>
     * @throws IOException
     */
    protected void writeFlags(OutputStream os) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4 * 2);
        int flags = ((this.isComplex() ? MatLevel5DataTypes.mtFLAG_COMPLEX : 0) |
                     (this.isGlobal() ? MatLevel5DataTypes.mtFLAG_GLOBAL : 0) |
                     (this.isLogical() ? MatLevel5DataTypes.mtFLAG_LOGICAL : 0) & 0x0000f700) |
                    (this.type & 0xff);
        System.out.println(flags + " " + this.type + " " + MatLevel5DataTypes.matrixTypeToString(this.type));
        buffer.putInt(flags);

        if (this.isSparse())
            buffer.putInt(((MLSparse)this).getMaxNZ());
        else
            buffer.putInt(0);
        OSMatTag tag = new OSMatTag(MatLevel5DataTypes.miUINT32, buffer);
        tag.writeTo(os);
    }

    /**
     * Writes MATRIX dimensions into <code>OutputStream</code>.
     *
     * @param os <code>OutputStream</code>
     * @throws IOException
     */
    private void writeDimensions(OutputStream os) throws IOException {
        int[] dims = this.getDimensions();
        ByteBuffer buffer = ByteBuffer.allocate(4 * dims.length);

        for (int i = 0; i < dims.length; ++i)
            buffer.putInt(dims[i]);
        OSMatTag tag = new OSMatTag(MatLevel5DataTypes.miINT32, buffer);
        tag.writeTo(os);
    }

    /**
     * Writes MATRIX name into <code>OutputStream</code>.
     *
     * @param os <code>OutputStream</code>
     * @throws IOException
     */
    protected void writeName(OutputStream os) throws IOException {
        if (!this.isChild && !VariableUtils.IsVarName(this.name))
            throw new MatlabIOException("Invalid variable name: " + this.name);
        this.writeString(os, this.name);
    }

    /**
     * Writes a String into <code>OutputStream</code>.
     *
     * @param os <code>OutputStream</code>
     * @param data <code>String</code>
     * @throws IOException
     */
    protected void writeString(OutputStream os, String data) throws IOException {
        byte[] nameByteArray = data.getBytes();
        OSMatTag tag = new OSMatTag(MatLevel5DataTypes.miINT8, nameByteArray);
        tag.writeTo(os);
    }

    /**
     * Write the data section of the MATLAB array to a data output stream.
     *
     */
    abstract public void writeData(DataOutputStream dos) throws IOException;

    abstract public String contentToString();
}
