package com.jmatio.types;

/**
 * Class represents Int16 (short) array (matrix)
 * 
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLInt16 extends MLUInt16
{

    /**
     * Normally this constructor is used only by MatFileReader and MatFileWriter
     * 
     * @param name - array name
     * @param dims - array dimensions
     * @param type - array type: here <code>mxINT16_CLASS</code>
     * @param attributes - array flags
     */
    public MLInt16( String name, int[] dims, int type, int attributes )
    {
        super( name, dims, type, attributes );
    }
    /**
     * Create a <code>{@link MLInt16}</code> array with given name,
     * and dimensions.
     * 
     * @param name - array name
     * @param dims - array dimensions
     */
    public MLInt16(String name, int[] dims)
    {
        super(name, dims, MLArray.mxINT16_CLASS, 0);
    }
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a 2D real matrix from a one-dimensional packed array
     * 
     * @param name - array name
     * @param vals - One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m - Number of rows
     */
    public MLInt16(String name, Short[] vals, int m )
    {
        super(name, vals, m );
        this.type = MLArray.mxINT16_CLASS;
    }
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a 2D real matrix from <code>byte[][]</code>
     * 
     * Note: array is converted to Byte[]
     * 
     * @param name - array name
     * @param vals - two-dimensional array of values
     */
    public MLInt16( String name, short[][] vals )
    {
        this( name, short2DToShort(vals), vals.length );
    }
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a matrix from a one-dimensional packed array
     * 
     * @param name - array name
     * @param vals - One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m - Number of rows
     */
    public MLInt16(String name, short[] vals, int m)
    {
        this(name, castToShort( vals ), m );
    }

}
