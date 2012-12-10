package com.jmatio.common;

/**
 * MAT-file data types. This stores constants used when interacting with Level 5
 * MAT-files.
 *
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MatLevel5DataTypes {
    // MAT-File Data Types
    public static final int miUNKNOWN       = 0;
    public static final int miINT8          = 1;
    public static final int miUINT8         = 2;
    public static final int miINT16         = 3;
    /* Also used for character data that is not Unicode encoded. */
    public static final int miUINT16        = 4;
    public static final int miINT32         = 5;
    public static final int miUINT32        = 6;
    public static final int miSINGLE        = 7;
    // RESERVED                             = 8;
    public static final int miDOUBLE        = 9;
    // RESERVED                             = 10;
    // RESERVED                             = 11;
    public static final int miINT64         = 12;
    public static final int miUINT64        = 13;
    public static final int miMATRIX        = 14;
    public static final int miCOMPRESSED    = 15;
    public static final int miUTF8          = 16;
    /** Byte order specified by Endian Indicator. */
    public static final int miUTF16         = 17;
    /** Byte order specified by Endian Indicator. */
    public static final int miUTF32         = 18;

    public static final int miSIZE_INT32    = 4;
    public static final int miSIZE_INT16    = 2;
    public static final int miSIZE_INT8     = 1;
    public static final int miSIZE_UINT32   = 4;
    public static final int miSIZE_UINT16   = 2;
    public static final int miSIZE_UINT8    = 1;
    public static final int miSIZE_DOUBLE   = 8;
    public static final int miSIZE_CHAR     = 1;

    // Suppress default constructor for noninstantiability.
    private MatLevel5DataTypes() { }

    /**
     * Return number of bytes for given type.
     *
     * @param type <code>MatLevel5DataTypes</code>
     * @return the size of the type (in bytes)
     */
    public static int sizeOf(int type) {
        switch (type) {
            case MatLevel5DataTypes.miINT8:
                return miSIZE_INT8;
            case MatLevel5DataTypes.miUINT8:
                return miSIZE_UINT8;
            case MatLevel5DataTypes.miINT16:
                return miSIZE_INT16;
            case MatLevel5DataTypes.miUINT16:
                return miSIZE_UINT16;
            case MatLevel5DataTypes.miINT32:
                return miSIZE_INT32;
            case MatLevel5DataTypes.miUINT32:
                return miSIZE_UINT32;
            case MatLevel5DataTypes.miDOUBLE:
                return miSIZE_DOUBLE;
            default:
                return 1;
        }
    }
    /**
     * Get String representation of a data type
     *
     * @param type data type
     * @return String representation
     */
    public static String dataTypeToString(int type) {
        String s;
        switch (type) {
            case MatLevel5DataTypes.miUNKNOWN:
                s = "unknown";
                break;
            case MatLevel5DataTypes.miINT8:
                s = "int8";
                break;
            case MatLevel5DataTypes.miUINT8:
                s = "uint8";
                break;
            case MatLevel5DataTypes.miINT16:
                s = "int16";
                break;
            case MatLevel5DataTypes.miUINT16:
                s = "uint16";
                break;
            case MatLevel5DataTypes.miINT32:
                s = "int32";
                break;
            case MatLevel5DataTypes.miUINT32:
                s = "uint32";
                break;
            case MatLevel5DataTypes.miSINGLE:
                s = "single";
                break;
            case MatLevel5DataTypes.miDOUBLE:
                s = "double";
                break;
            case MatLevel5DataTypes.miINT64:
                s = "int64";
                break;
            case MatLevel5DataTypes.miUINT64:
                s = "uint64";
                break;
            case MatLevel5DataTypes.miMATRIX:
                s = "matrix";
                break;
            case MatLevel5DataTypes.miCOMPRESSED:
                s = "compressed";
                break;
            case MatLevel5DataTypes.miUTF8:
                s = "uft8";
                break;
            case MatLevel5DataTypes.miUTF16:
                s = "utf16";
                break;
            case MatLevel5DataTypes.miUTF32:
                s = "utf32";
                break;
            default:
                s = "unknown";
        }
        return s;
    }
    
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
    public static final String matrixTypeToString(int type) {
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
    
    public static boolean isComplex(int flags) {
        return (flags & MatLevel5DataTypes.mtFLAG_COMPLEX) != 0;
    }

    public static boolean isGlobal(int flags) {
        return (flags & MatLevel5DataTypes.mtFLAG_GLOBAL) != 0;
    }

    public static boolean isLogical(int flags) {
        return (flags & MatLevel5DataTypes.mtFLAG_LOGICAL) != 0;
    }

}
