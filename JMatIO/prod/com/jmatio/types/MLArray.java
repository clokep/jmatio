package com.jmatio.types;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.jmatio.common.MatDataTypes;
import com.jmatio.common.VariableUtils;
import com.jmatio.io.OSMatTag;
import com.jmatio.io.MatlabIOException;

public abstract class MLArray {
    /* Matlab Array Types (Classes) */
    /** This is undocumented. */
    public static final int mxUNKNOWN_CLASS     = 0;
    /** Cell array. */
    public static final int mxCELL_CLASS        = 1;
    /** Structure. */
    public static final int mxSTRUCT_CLASS      = 2;
    /** Object. */
    public static final int mxOBJECT_CLASS      = 3;
    /** Character array. */
    public static final int mxCHAR_CLASS        = 4;
    /** Sparse array. */
    public static final int mxSPARSE_CLASS      = 5;
    /** Double precision array. */
    public static final int mxDOUBLE_CLASS      = 6;
    /** Single precision array. */
    public static final int mxSINGLE_CLASS      = 7;
    /** 8-bit, signed integer. */
    public static final int mxINT8_CLASS        = 8;
    /** 8-bit, unsigned integer. */
    public static final int mxUINT8_CLASS       = 9;
    /** 16-bit, signed integer. */
    public static final int mxINT16_CLASS       = 10;
    /** 16-bit, unsigned integer. */
    public static final int mxUINT16_CLASS      = 11;
    /** 32-bit, signed integer. */
    public static final int mxINT32_CLASS       = 12;
    /** 32-bit, unsigned integer. */
    public static final int mxUINT32_CLASS      = 13;
    /** 64-bit, signed integer. */
    public static final int mxINT64_CLASS       = 14;
    /** 64-bit, unsigned integer. */
    public static final int mxUINT64_CLASS      = 15;
    /** This is undocumented. */
    public static final int mxFUNCTION_CLASS    = 16;
    /** This is undocumented. */
    public static final int mxOPAQUE_CLASS      = 17;

    /** The data element includes an imaginary part (pi). */
    public static final int mtFLAG_COMPLEX      = 0x0800;
    /** MATLAB loads the data element as a global variable in the base workspace. */
    public static final int mtFLAG_GLOBAL       = 0x0400;
    /** The array is used for logical indexing. */
    public static final int mtFLAG_LOGICAL      = 0x0200;
    public static final int mtFLAG_TYPE         = 0xff;

    /** The dimensions of the array. */
    protected int dims[];
    /** The array name. */
    public String name;
    /** The attributes of the array (e.g. complex, global, logical). */
    protected int attributes;
    /** The type of data stored in the array. */
    protected int type;

    /** This is true for sub-elements of structs & cells where name does not matter. */
    public boolean isChild = false;

    public MLArray(String name, int[] dims, int type, int attributes) {
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

        this.type = type;
        this.attributes = attributes;
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
     * Gets the array flags.
     *
     * @return array flags
     */
    public int getFlags() {
        return this.type & MLArray.mtFLAG_TYPE | this.attributes & 0xffffff00;
    }

    /**
     * Gets the array name as an array of bytes.
     *
     * @return array name as bytes
     */
    public byte[] getNameToByteArray() {
        return this.name.getBytes();
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

    public static final String typeToString(int type) {
        String s;
        switch (type) {
            case mxUNKNOWN_CLASS:
                s = "unknown";
                break;
            case mxCELL_CLASS:
                s = "cell";
                break;
            case mxSTRUCT_CLASS:
                s = "struct";
                break;
            case mxCHAR_CLASS:
                s = "char";
                break;
            case mxSPARSE_CLASS:
                s = "sparse";
                break;
            case mxDOUBLE_CLASS:
                s = "double";
                break;
            case mxSINGLE_CLASS:
                s = "single";
                break;
            case mxINT8_CLASS:
                s = "int8";
                break;
            case mxUINT8_CLASS:
                s = "uint8";
                break;
            case mxINT16_CLASS:
                s = "int16";
                break;
            case mxUINT16_CLASS:
                s = "uint16";
                break;
            case mxINT32_CLASS:
                s = "int32";
                break;
            case mxUINT32_CLASS:
                s = "uint32";
                break;
            case mxINT64_CLASS:
                s = "int64";
                break;
            case mxUINT64_CLASS:
                s = "uint64";
                break;
            case mxFUNCTION_CLASS:
                s = "function_handle";
                break;
            case mxOPAQUE_CLASS:
                s = "opaque";
                break;
            case mxOBJECT_CLASS:
                s = "object";
                break;
            default:
                s = "unknown";
                break;
        }
        return s;
    }

    public boolean isCell() {
        return this.type == MLArray.mxCELL_CLASS;
    }

    public boolean isChar() {
        return this.type == MLArray.mxCHAR_CLASS;
    }

    public boolean isComplex() {
        return (this.attributes & MLArray.mtFLAG_COMPLEX) != 0;
    }

    public boolean isSparse() {
        return this.type == MLArray.mxSPARSE_CLASS;
    }

    public boolean isStruct() {
        return this.type == MLArray.mxSTRUCT_CLASS;
    }

    public boolean isDouble() {
        return this.type == MLArray.mxDOUBLE_CLASS;
    }

    public boolean isSingle() {
        return this.type == MLArray.mxSINGLE_CLASS;
    }

    public boolean isInt8() {
        return this.type == MLArray.mxINT8_CLASS;
    }

    public boolean isUint8() {
        return this.type == MLArray.mxUINT8_CLASS;
    }

    public boolean isInt16() {
        return this.type == MLArray.mxINT16_CLASS;
    }

    public boolean isUint16() {
        return this.type == MLArray.mxUINT16_CLASS;
    }

    public boolean isInt32() {
        return this.type == MLArray.mxINT32_CLASS;
    }

    public boolean isUint32() {
        return this.type == MLArray.mxUINT32_CLASS;
    }

    public boolean isInt64(){
        return this.type == MLArray.mxINT64_CLASS;
    }

    public boolean isUint64() {
        return this.type == MLArray.mxUINT64_CLASS;
    }

    public boolean isObject() {
        return this.type == MLArray.mxOBJECT_CLASS;
    }

    public boolean isOpaque() {
        return this.type == MLArray.mxOPAQUE_CLASS;
    }

    public boolean isLogical() {
        return (this.attributes & MLArray.mtFLAG_LOGICAL) != 0;
    }

    public boolean isFunctionObject() {
        return this.type == MLArray.mxFUNCTION_CLASS;
    }

    public boolean isUnknown() {
        return this.type == MLArray.mxUNKNOWN_CLASS;
    }

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
            sb.append(this.typeToString(this.type));
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

    public String contentToString() {
        return "content cannot be displayed";
    }

    abstract public void dispose();

    /**
     * Writes MATRIX into <code>OutputStream</code>.
     *
     * @param output <code>OutputStream</code>
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
        OSMatTag tag = new OSMatTag(MatDataTypes.miMATRIX, baos.toByteArray());
        tag.writeTo(os);
    }

    /**
     * Writes MATRIX flags into <code>OutputStream</code>.
     *
     * @param os <code>OutputStream</code>
     * @throws IOException
     */
    private void writeFlags(OutputStream os) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4 * 2);
        buffer.putInt(this.getFlags());

        if (this.isSparse())
            buffer.putInt(((MLSparse)this).getMaxNZ());
        else
            buffer.putInt(0);
        OSMatTag tag = new OSMatTag(MatDataTypes.miUINT32, buffer);
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
        OSMatTag tag = new OSMatTag(MatDataTypes.miINT32, buffer);
        tag.writeTo(os);
    }

    /**
     * Writes MATRIX name into <code>OutputStream</code>.
     *
     * @param os <code>OutputStream</code>
     * @throws IOException
     */
    private void writeName(OutputStream os) throws IOException {
        if (!this.isChild && !VariableUtils.IsVarName(this.name))
            throw new MatlabIOException("Invalid variable name: " + this.name);

        byte[] nameByteArray = this.getNameToByteArray();
        OSMatTag tag = new OSMatTag(MatDataTypes.miINT8, nameByteArray);
        tag.writeTo(os);
    }

    /**
     * Write the data section of the MATLAB array to a data output stream.
     *
     */
    abstract public void writeData(DataOutputStream dos) throws IOException;
}
