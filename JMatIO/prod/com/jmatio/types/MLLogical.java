package com.jmatio.types;

/**
 * 
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLLogical extends MLUInt8
{
    /**
     * Normally this constructor is used only by MatFileReader and MatFileWriter
     * 
     * @param name array name
     * @param dims array dimensions
     * @param type array type: here <code>mxUINT8_CLASS</code>
     * @param attributes array flags: here <code>mtFLAG_LOGICAL</code>
     */
    public MLLogical( String name, int[] dims, int type, int attributes )
    {
        super( name, dims, type, attributes );
    }
    /**
     * Create a <code>{@link MLLogical}</code> array with given name,
     * and dimensions.
     * 
     * @param name array name
     * @param dims array dimensions
     */
    public MLLogical(String name, int[] dims)
    {
        super(name, dims, MLArray.mxUINT8_CLASS, MLArray.mtFLAG_LOGICAL);
    }
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a 2D real matrix from a one-dimensional packed array
     * 
     * @param name array name
     * @param vals One-dimensional array of boolean, packed by columns (ala Fortran).
     * @param m Number of rows
     */
    public MLLogical(String name, boolean[] vals, int m )
    {
        super(name, castToByte( vals ), m );
        this.attributes |= MLArray.mtFLAG_LOGICAL;
    }
    
    /**
     * @param vector
     */
    public void set(boolean[] vector)
    {
    	set(castToByte( vector ));
    }
    /**
     * Casts <code>Double[]</code> to <code>byte[]</code>
     * 
     * @param d <code>Long[]</code>
     * @return result <code>long[]</code>
     */
    protected static Byte[] castToByte( boolean[] d )
    {
    	Byte[] dest = new Byte[d.length];
        for ( int i = 0; i < d.length; i++ )
        {
            dest[i] = d[i] ? (byte)1 : (byte)0;
        }
        return dest;
    }
}
