package com.jmatio.types;

import java.nio.ByteBuffer;

/**
 * Class represents UInt32 (int) array (matrix)
 * 
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLUInt32 extends MLNumericArray<Integer>
{

    /**
     * Normally this constructor is used only by MatFileReader and MatFileWriter
     * 
     * @param name - array name
     * @param dims - array dimensions
     * @param type - array type: here <code>mxUINT32_CLASS</code>
     * @param attributes - array flags
     */
    public MLUInt32( String name, int[] dims, int type, int attributes )
    {
        super( name, dims, type, attributes );
    }
    /**
     * Create a <code>{@link MLUInt32}</code> array with given name,
     * and dimensions.
     * 
     * @param name - array name
     * @param dims - array dimensions
     */
    public MLUInt32(String name, int[] dims)
    {
        super(name, dims, MLArray.mxUINT32_CLASS, 0);
    }
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a 2D real matrix from a one-dimensional packed array
     * 
     * @param name - array name
     * @param vals - One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m - Number of rows
     */
    public MLUInt32(String name, Integer[] vals, int m )
    {
        super(name, MLArray.mxUINT32_CLASS, vals, m );
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
    public MLUInt32( String name, int[][] vals )
    {
        this( name, int2DToInteger(vals), vals.length );
    }
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a matrix from a one-dimensional packed array
     * 
     * @param name - array name
     * @param vals - One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m - Number of rows
     */
    public MLUInt32(String name, int[] vals, int m)
    {
        this(name, castToInteger( vals ), m );
    }
    /**
     * @param vector
     */
    public void set(int[] vector)
    {
    	set(castToInteger( vector ));
    }
    /* (non-Javadoc)
     * @see com.jmatio.types.GenericArrayCreator#createArray(int, int)
     */
    public Integer[] createArray(int m, int n)
    {
        return new Integer[m*n];
    }
    /**
     * Gets two-dimensional real array.
     * 
     * @return - 2D real array
     */
    public int[][] getArray()
    {
        final int M = getM();
        final int N = getN();
        int[][] result = new int[M][];
        
        for ( int m = 0; m < M; m++ )
        {
           result[m] = new int[ N ];

           for ( int n = 0; n < N; n++ )
           {               
               result[m][n] = getReal(m,n);
           }
        }
        return result;
    }
    /**
     * Casts <code>int[]</code> to <code>Integer[]</code>
     * 
     * @param - source <code>int[]</code>
     * @return - result <code>Integer[]</code>
     */
    protected static Integer[] castToInteger( int[] d )
    {
    	Integer[] dest = new Integer[d.length];
        for ( int i = 0; i < d.length; i++ )
        {
            dest[i] = (Integer)d[i];
        }
        return dest;
    }
    /**
     * Converts <code>int[][]</code> to <code>Integer[]</code>
     * 
     * @param dd
     * @return
     */
    protected static Integer[] int2DToInteger ( int[][] dd )
    {
    	Integer[] d = new Integer[ dd.length*dd[0].length ];
        for ( int n = 0; n < dd[0].length; n++ )
        {
            for ( int m = 0; m < dd.length; m++ )
            {
                d[ m+n*dd.length ] = dd[m][n]; 
            }
        }
        return d;
    }
    public Integer buldFromBytes(byte[] bytes)
    {
        if ( bytes.length != getBytesAllocated() )
        {
            throw new IllegalArgumentException( 
                        "To build from byte array I need array of size: " 
                                + getBytesAllocated() );
        }
        return ByteBuffer.wrap( bytes ).getInt();
    }
    public int getBytesAllocated()
    {
        return Integer.SIZE >> 3;
    }
    
    public Class<Integer> getStorageClazz()
    {
        return Integer.class;
    }
    public byte[] getByteArray(Integer value)
    {
        int byteAllocated = getBytesAllocated();
        ByteBuffer buff = ByteBuffer.allocate( byteAllocated );
        buff.putInt( value );
        return buff.array();
    }
    

}
