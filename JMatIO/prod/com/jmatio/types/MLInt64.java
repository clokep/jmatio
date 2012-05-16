package com.jmatio.types;

/**
 * Class represents Int64 (long) array (matrix)
 * 
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MLInt64 extends MLUInt64
{

    /**
     * Normally this constructor is used only by MatFileReader and MatFileWriter
     * 
     * @param name array name
     * @param dims array dimensions
     * @param type array type: here <code>mxINT64_CLASS</code>
     * @param attributes array flags
     */
    public MLInt64( String name, int[] dims, int type, int attributes )
    {
        super( name, dims, type, attributes );
    }
    /**
     * Create a <code>{@link MLInt64}</code> array with given name,
     * and dimensions.
     * 
     * @param name array name
     * @param dims array dimensions
     */
    public MLInt64(String name, int[] dims)
    {
        super(name, dims, MLArray.mxINT64_CLASS, 0);
    }
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a 2D real matrix from a one-dimensional packed array
     * 
     * @param name array name
     * @param vals One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m Number of rows
     */
    public MLInt64(String name, Long[] vals, int m )
    {
        super(name, vals, m );
        this.type = MLArray.mxINT64_CLASS;
    }
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a 2D real matrix from <code>byte[][]</code>
     * 
     * Note: array is converted to Byte[]
     * 
     * @param name array name
     * @param vals two-dimensional array of values
     */
    public MLInt64( String name, long[][] vals )
    {
        this( name, long2DToLong(vals), vals.length );
    }
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a matrix from a one-dimensional packed array
     * 
     * @param name array name
     * @param vals One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m Number of rows
     */
    public MLInt64(String name, long[] vals, int m)
    {
        this(name, castToLong( vals ), m );
    }

}
