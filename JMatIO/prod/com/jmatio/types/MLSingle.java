package com.jmatio.types;

import java.nio.ByteBuffer;

/**
 * Class represents Single array (matrix)
 * 
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MLSingle extends MLNumericArray<Float>
{
    
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a 2D real matrix from a one-dimensional packed array
     * 
     * @param name array name
     * @param vals One-dimensional array of floats, packed by columns (ala Fortran).
     * @param m Number of rows
     */
    public MLSingle(String name, Float[] vals, int m)
    {
        super(name, MLArray.mxSINGLE_CLASS, vals, m);
    }

    /**
     * Normally this constructor is used only by MatFileReader and MatFileWriter
     * 
     * @param name array name
     * @param dims array dimensions
     * @param type array type: here <code>mxSINGLE_CLASS</code>
     * @param attributes array flags
     */
    public MLSingle(String name, int[] dims, int type, int attributes)
    {
        super(name, dims, type, attributes);
    }

    /**
     * Create a <code>MLSingle</code> array with given name,
     * and dimensions.
     * 
     * @param name array name
     * @param dims array dimensions
     */
    public MLSingle(String name, int[] dims)
    {
        super(name, dims, MLArray.mxSINGLE_CLASS, 0);
    }

    /**
     * @param vector
     */
    public void set(float[] vector)
    {
    	set(castToFloat( vector ));
    }

    /* (non-Javadoc)
     * @see com.jmatio.types.GenericArrayCreator#createArray(int, int)
     */
    public Float[] createArray(int m, int n)
    {
        return new Float[m*n];
    }

    /**
     * Casts <code>float[]</code> to <code>Float[]</code>
     *
     * @param source <code>float[]</code>
     * @return result <code>Float[]</code>
     */
    private static Float[] castToFloat( float[] f )
    {
    	Float[] dest = new Float[f.length];
        for ( int i = 0; i < f.length; i++ )
        {
            dest[i] = (Float)f[i];
        }
        return dest;
    }

    public Float buldFromBytes(byte[] bytes)
    {
        if ( bytes.length != getBytesAllocated() )
        {
            throw new IllegalArgumentException( 
                        "To build from byte array I need array of size: " 
                                + getBytesAllocated() );
        }
        return ByteBuffer.wrap( bytes ).getFloat();
    }

    public byte[] getByteArray(Float value)
    {
        int byteAllocated = getBytesAllocated();
        ByteBuffer buff = ByteBuffer.allocate( byteAllocated );
        buff.putFloat( value );
        return buff.array();
    }

    public int getBytesAllocated()
    {
        return Float.SIZE >> 3;
    }

    public Class<?> getStorageClazz()
    {
        return Float.class;
    }

}
