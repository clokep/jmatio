package com.jmatio.types;

import java.nio.ByteBuffer;

/**
 * Class represents UInt16 (short) array (matrix)
 * 
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLUInt16 extends MLNumericArray<Short>
{

    /**
     * Normally this constructor is used only by MatFileReader and MatFileWriter
     * 
     * @param name - array name
     * @param dims - array dimensions
     * @param type - array type: here <code>mxUINT16_CLASS</code>
     * @param attributes - array flags
     */
    public MLUInt16( String name, int[] dims, int type, int attributes )
    {
        super( name, dims, type, attributes );
    }
    /**
     * Create a <code>{@link MLUInt16}</code> array with given name,
     * and dimensions.
     * 
     * @param name - array name
     * @param dims - array dimensions
     */
    public MLUInt16(String name, int[] dims)
    {
        super(name, dims, MLArray.mxUINT16_CLASS, 0);
    }
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a 2D real matrix from a one-dimensional packed array
     * 
     * @param name - array name
     * @param vals - One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m - Number of rows
     */
    public MLUInt16(String name, Short[] vals, int m )
    {
        super(name, MLArray.mxUINT16_CLASS, vals, m );
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
    public MLUInt16( String name, short[][] vals )
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
    public MLUInt16(String name, short[] vals, int m)
    {
        this(name, castToShort( vals ), m );
    }
    /**
     * @param vector
     */
    public void set(short[] vector)
    {
    	set(castToShort( vector ));
    }
    /* (non-Javadoc)
     * @see com.jmatio.types.GenericArrayCreator#createArray(int, int)
     */
    public Short[] createArray(int m, int n)
    {
        return new Short[m*n];
    }
    /**
     * Gets two-dimensional real array.
     * 
     * @return - 2D real array
     */
    public int[][] getArray()
    {
    	int[][] result = new int[getM()][];
        
        for ( int m = 0; m < getM(); m++ )
        {
           result[m] = new int[ getN() ];

           for ( int n = 0; n < getN(); n++ )
           {               
               result[m][n] = getReal(m,n);
           }
        }
        return result;
    }
    /**
     * Casts <code>short[]</code> to <code>Short[]</code>
     * 
     * @param - source <code>short[]</code>
     * @return - result <code>Short[]</code>
     */
    protected static Short[] castToShort( short[] d )
    {
    	Short[] dest = new Short[d.length];
        for ( int i = 0; i < d.length; i++ )
        {
            dest[i] = (Short)d[i];
        }
        return dest;
    }
    /**
     * Converts <code>short[][]</code> to <code>Short[]</code>
     * 
     * @param dd
     * @return
     */
    protected static Short[] short2DToShort ( short[][] dd )
    {
    	Short[] d = new Short[ dd.length*dd[0].length ];
        for ( int n = 0; n < dd[0].length; n++ )
        {
            for ( int m = 0; m < dd.length; m++ )
            {
                d[ m+n*dd.length ] = dd[m][n]; 
            }
        }
        return d;
    }
    public Short buldFromBytes(byte[] bytes)
    {
        if ( bytes.length != getBytesAllocated() )
        {
            throw new IllegalArgumentException( 
                        "To build from byte array I need array of size: " 
                                + getBytesAllocated() );
        }
        return ByteBuffer.wrap( bytes ).getShort();
    }
    public int getBytesAllocated()
    {
        return Short.SIZE >> 3;
    }
    
    public Class<Short> getStorageClazz()
    {
        return Short.class;
    }
    public byte[] getByteArray(Short value)
    {
        int byteAllocated = getBytesAllocated();
        ByteBuffer buff = ByteBuffer.allocate( byteAllocated );
        buff.putShort( value );
        return buff.array();
    }
    

}
