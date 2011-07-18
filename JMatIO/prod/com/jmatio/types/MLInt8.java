package com.jmatio.types;

public class MLInt8 extends MLUInt8
{
    /**
     * Normally this constructor is used only by MatFileReader and MatFileWriter
     * 
     * @param name - array name
     * @param dims - array dimensions
     * @param type - array type: here <code>mxINT8_CLASS</code>
     * @param attributes - array flags
     */
    public MLInt8( String name, int[] dims, int type, int attributes )
    {
        super( name, dims, type, attributes );
    }
    /**
     * Create a <code>{@link MLUInt8}</code> array with given name,
     * and dimensions.
     * 
     * @param name - array name
     * @param dims - array dimensions
     */
    public MLInt8(String name, int[] dims)
    {
        super(name, dims, MLArray.mxINT8_CLASS, 0);
    }
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a 2D real matrix from a one-dimensional packed array
     * 
     * @param name - array name
     * @param vals - One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m - Number of rows
     */
    public MLInt8(String name, Byte[] vals, int m )
    {
        super(name, vals, m );
        this.type = MLArray.mxINT8_CLASS;
    }
    }
