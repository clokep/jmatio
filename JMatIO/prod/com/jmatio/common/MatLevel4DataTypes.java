package com.jmatio.common;

/**
 * MAT-file data types.
 *
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MatLevel4DataTypes {
    // The data format stored in the field (the "P" value).
    /** Double-precision (64-bit) floating-point numbers. */
    public static final int miDOUBLE = 0;
    /** Single-precision (32-bit) floating-point numbers. */
    public static final int miSINGLE = 1;
    /** 32-bit signed integers. */
    public static final int miINT32  = 2;
    /** 16-bit signed integers. */
    public static final int miINT16  = 3;
    /** 16-bit unsigned integers. */
    public static final int miUINT16 = 4;
    /** 8-bit unsigned integers. */
    public static final int miUINT8  = 5;

    public static final int miSIZE_INT32    = 4;
    public static final int miSIZE_INT16    = 2;
    public static final int miSIZE_UINT16   = 2;
    public static final int miSIZE_UINT8    = 1;
    public static final int miSIZE_DOUBLE   = 8;
    public static final int miSIZE_SINGLE   = 4;

    // Suppress default constructor for noninstantiability.
    private MatLevel4DataTypes() { }

    /**
     * Return number of bytes for given type.
     *
     * @param type <code>MatLevel4DataTypes</code>
     * @return the size of the type (in bytes)
     */
    public static int sizeOf(int type) {
        switch (type) {
            case MatLevel4DataTypes.miUINT8:
                return miSIZE_UINT8;
            case MatLevel4DataTypes.miINT16:
                return miSIZE_INT16;
            case MatLevel4DataTypes.miUINT16:
                return miSIZE_UINT16;
            case MatLevel4DataTypes.miINT32:
                return miSIZE_INT32;
            case MatLevel4DataTypes.miDOUBLE:
                return miSIZE_DOUBLE;
            case MatLevel4DataTypes.miSINGLE:
                return miSIZE_SINGLE;
            default:
                // XXX Throw here.
                return 1;
        }
    }

    /**
     * Get String representation of a data type
     *
     * @param type data type
     * @return String representation
     */
    public static String typeToString(int type) {
        String s;
        switch (type) {
            case MatLevel4DataTypes.miUINT8:
                s = "uint8";
                break;
            case MatLevel4DataTypes.miINT16:
                s = "int16";
                break;
            case MatLevel4DataTypes.miUINT16:
                s = "uint16";
                break;
            case MatLevel4DataTypes.miINT32:
                s = "int32";
                break;
            case MatLevel4DataTypes.miSINGLE:
                s = "single";
                break;
            case MatLevel4DataTypes.miDOUBLE:
                s = "double";
                break;
            default:
                s = "unknown";
        }
        return s;
    }
    
    /* "T" indicates the matrix type. */
    /** Numeric (Full) matrix. */
    public static final int mxNUMERIC_CLASS = 0;
    /** Text matrix. */
    public static final int mxTEXT_CLASS    = 1;
    /** Sparse matrix. */
    public static final int mxSPARSE_CLASS  = 2;
}
