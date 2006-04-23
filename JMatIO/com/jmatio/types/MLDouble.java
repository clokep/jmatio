package com.jmatio.types;

/**
 * Class represents Double array (matrix)
 * 
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MLDouble extends MLNumericArray<Double>
{

    /**
     * Normally this constructor is used only by MatFileReader and MatFileWriter
     * 
     * @param name - array name
     * @param dims - array dimensions
     * @param type - array type: here <code>mxDOUBLE_CLASS</code>
     * @param attributes - array flags
     */
    public MLDouble( String name, int[] dims, int type, int attributes )
    {
        super( name, dims, type, attributes );
    }
    /**
     * Create a <code>MLDouble</code> array with given name,
     * and dimensions.
     * 
     * @param name - array name
     * @param dims - array dimensions
     */
    public MLDouble(String name, int[] dims)
    {
        super(name, dims, MLArray.mxDOUBLE_CLASS, 0);
    }
    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a 2D real matrix from a one-dimensional packed array
     * 
     * @param name - array name
     * @param vals - One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m - Number of rows
     */
    public MLDouble(String name, Double[] vals, int m )
    {
        super(name, MLArray.mxDOUBLE_CLASS, vals, m );
    }

    /**
     * <a href="http://math.nist.gov/javanumerics/jama/">Jama</a> [math.nist.gov] style: 
     * construct a matrix from a one-dimensional packed array
     * 
     * @param name - array name
     * @param vals - One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m - Number of rows
     */
    public MLDouble(String name, double[] vals, int m)
    {
        this(name, castToDouble( vals ), m );
    }
    /* (non-Javadoc)
     * @see com.jmatio.types.GenericArrayCreator#createArray(int, int)
     */
    public Double[] createArray(int m, int n)
    {
        return new Double[m*n];
    }
    /**
     * Gets two-dimensional real array.
     * 
     * @return - 2D real array
     */
    public double[][] getArray()
    {
        double[][] result = new double[getM()][];
        
        for ( int m = 0; m < getM(); m++ )
        {
           result[m] = new double[ getN() ];

           for ( int n = 0; n < getN(); n++ )
           {               
               result[m][n] = getReal(m,n);
           }
        }
        return result;
    }
    /**
     * Casts <code>Double[]</code> to <code>double[]</code>
     * 
     * @param - source <code>Double[]</code>
     * @return - result <code>double[]</code>
     */
    private static Double[] castToDouble( double[] d )
    {
        Double[] dest = new Double[d.length];
        for ( int i = 0; i < d.length; i++ )
        {
            dest[i] = (Double)d[i];
        }
        return dest;
    }




}
