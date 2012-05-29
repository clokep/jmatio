package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.jmatio.io.MatFileFilter;
import com.jmatio.io.MatFileIncrementalWriter;
import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt64;
import com.jmatio.types.MLInt8;
import com.jmatio.types.MLLogical;
import com.jmatio.types.MLNumericArray;
import com.jmatio.types.MLSingle;
import com.jmatio.types.MLSparse;
import com.jmatio.types.MLStructure;
import com.jmatio.types.MLUInt64;
import com.jmatio.types.MLUInt8;

/**
 * The test suite for JMatIO
 *
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MatIOTest
{
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter( MatIOTest.class );
    }

//    @Test
//    public void testMLDoubleConstructor(){
//    	// Test whether the constuctor can handle an array with a length != a multiple of dsize
//    	// BufferOverflowException was thrown before a fix in MLNumericArray
//    	int dsize=2;
//		List<Double> list = new ArrayList<Double>();
//		list.add(1.0);
//
//		new MLDouble("name",(Double[])list.toArray(new Double[list.size()]), dsize);
//    }

    //@Test
    public void testBenchmarkDouble() throws Exception
    {
        final String fileName = "bb.mat";
        final String name = "bigdouble";
        final int SIZE = 1000;
        //System.out.println(14e6);
        ByteBuffer.allocateDirect(1000000000);

        MLDouble mlDouble = new MLDouble( name, new int[] {SIZE, SIZE} );

        for ( int i = 0; i < SIZE*SIZE; i++ )
        {
            mlDouble.set((double)i, i);
        }


        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mlDouble );

        //write arrays to file
        new MatFileWriter( fileName, list );

        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLArray mlArrayRetrived = mfr.getMLArray( name );
//
//        System.out.println( mlArrayRetrived );
//        System.out.println( mlArrayRetrived.contentToString() );

        //test if MLArray objects are equal
        assertEquals("Test if value red from file equals value stored", mlDouble, mlArrayRetrived);
    }


    @Test
    public void testBenchmarkUInt8() throws Exception
    {
        final String fileName = "bigbyte.mat";
        final String name = "bigbyte";
        final int SIZE = 1024;


        MLUInt8 mluint8 = new MLUInt8( name, new int[] { SIZE, SIZE } );

        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mluint8 );

        //write arrays to file
        new MatFileWriter( fileName, list );

        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLArray mlArrayRetrived = mfr.getMLArray( name );

        final long start = System.nanoTime();
        for ( int i = 0; i < mlArrayRetrived.getSize(); i++ )
        {
            ((MLNumericArray<?>)mlArrayRetrived).get(i);
        }
        final long stop = System.nanoTime();
        System.out.println("--> " + (stop - start)/1e6 +  "[ns]");

        //test if MLArray objects are equal
        assertEquals("Test if value red from file equals value stored", mluint8, mlArrayRetrived);
    }

    /**
     * Tests filtered reading
     *
     * @throws IOException
     */
    @Test
    public void testFilteredReading() throws IOException
    {
        //1. First create arrays
        //array name
        String name = "doublearr";
        String name2 = "dummy";
        //file name in which array will be storred
        String fileName = "filter.mat";

        double[] src = new double[] { 1.3, 2.0, 3.0, 4.0, 5.0, 6.0 };
        MLDouble mlDouble = new MLDouble( name, src, 3 );
        MLChar mlChar = new MLChar( name2, "I am dummy" );

        //2. write arrays to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mlDouble );
        list.add( mlChar );
        new MatFileWriter( fileName, list );

        //3. create new filter instance
        MatFileFilter filter = new MatFileFilter();
        filter.addArrayName( name );

        //4. read array form file
        MatFileReader mfr = new MatFileReader( fileName, filter );

        //check size of
        Map<String, MLArray> content = mfr.getContent();
        assertEquals("Test if only one array was red", 1, content.size() );

    }
    /**
     * Test <code>MatFileFilter</code> options
     */
    @Test
    public void testMatFileFilter()
    {
        //create new filter instance
        MatFileFilter filter = new MatFileFilter();

        //empty filter should match all patterns
        assertEquals("Test if empty filter matches all patterns", true, filter.matches("any") );

        //now add something to the filter
        filter.addArrayName("my_array");

        //test if filter matches my_array
        assertEquals("Test if filter matches given array name", true, filter.matches("my_array") );

        //test if filter returns false if does not match given name
        assertEquals("Test if filter does not match non existent name", false, filter.matches("dummy") );

    }

    /**
     * Tests <code>MLChar</code> reading and writing.
     *
     * @throws IOException
     */
    @Test
    public void testMLCharArray() throws IOException
    {
        //array name
        String name = "chararr";
        //file name in which array will be storred
        String fileName = "mlchar.mat";
        //temp
        String valueS;

        //create MLChar array of a name "chararr" containig one
        //string value "dummy"
        MLChar mlChar = new MLChar(name, "dummy");

        //get array name
        valueS = mlChar.getName();
        assertEquals("MLChar name getter", name, valueS);

        //get value of the first element
        valueS = mlChar.getString(0);
        assertEquals("MLChar value getter", "dummy", valueS);

        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mlChar );

        //write arrays to file
        new MatFileWriter( fileName, list );

        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLArray mlCharRetrived = mfr.getMLArray( name );

        assertEquals("Test if value red from file equals value stored", mlChar, mlCharRetrived);

        //try to read non existent array
        mlCharRetrived = mfr.getMLArray( "nonexistent" );
        assertEquals("Test if non existent value is null", null, mlCharRetrived);
    }

    /**
     * Tests <code>MLDouble</code> reading and writing.
     *
     * @throws IOException
     */
    @Test
    public void testMLDoubleArray() throws IOException
    {
        //array name
        String name = "doublearr";
        //file name in which array will be storred
        String fileName = "mldouble.mat";

        //test column-packed vector
        double[] src = new double[] { 1.3, 2.0, 3.0, 4.0, 5.0, 6.0 };
        //test 2D array coresponding to test vector
        double[][] src2D = new double[][] { { 1.3, 4.0 },
                                            { 2.0, 5.0 },
                                            { 3.0, 6.0 }
                                        };

        //create 3x2 double matrix
        //[ 1.0 4.0 ;
        //  2.0 5.0 ;
        //  3.0 6.0 ]
        MLDouble mlDouble = new MLDouble( name, src, 3 );

        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mlDouble );

        //write arrays to file
        new MatFileWriter( fileName, list );

        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLArray mlArrayRetrived = mfr.getMLArray( name );

        //System.out.println( mlDouble.contentToString() );
        //System.out.println( mlArrayRetrived.contentToString() );
        //test if MLArray objects are equal
        assertEquals("Test if value red from file equals value stored", mlDouble, mlArrayRetrived);

        //test if 2D array match
        for ( int i = 0; i < src2D.length; i++ )
        {
            boolean result = Arrays.equals( src2D[i], ((MLDouble)mlArrayRetrived ).getArray()[i] );
            assertEquals( "2D array match", true, result );
        }

        //test new constructor
        MLArray mlDouble2D = new MLDouble(name, src2D );
        //compare it with original
        assertEquals( "Test if double[][] constructor produces the same matrix as normal one", mlDouble2D, mlDouble );
    }

    /**
     * Regression bug
     *
     * @throws Exception
     */
    @Test
    public void testDoubleFromMatlabCreatedFile() throws Exception
    {
        //array name
        String name = "arr";
        //file name in which array will be stored
        String fileName = "test/matnativedouble.mat";

        //test column-packed vector
        double[] src = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
        MLDouble mlDouble = new MLDouble( name, src, 3 );

        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLArray mlArrayRetrived = mfr.getMLArray( name );

        //test if MLArray objects are equal
        assertEquals("Test if value red from file equals value stored", mlDouble, mlArrayRetrived);
    }

    /**
     * Regression bug.

     * <pre><code>
     * Matlab code:
     * >> arr = [1.1, 4.4; 2.2, 5.5; 3.3, 6.6];
     * >> save('matnativedouble2', arr);
     * </code></pre>
     *
     * @throws IOException
     */
    @Test
    public void testDoubleFromMatlabCreatedFile2() throws IOException
    {
        //array name
        String name = "arr";
        //file name in which array will be stored
        String fileName = "test/matnativedouble2.mat";

        //test column-packed vector
        double[] src = new double[] { 1.1, 2.2, 3.3, 4.4, 5.5, 6.6 };
        MLDouble mlDouble = new MLDouble( name, src, 3 );

        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLArray mlArrayRetrived = mfr.getMLArray( name );

        //test if MLArray objects are equal
        assertEquals("Test if value red from file equals value stored", mlDouble, mlArrayRetrived);
    }

    /**
     * Regression bug: Test writing several arrays into a single file.
     *
     * @throws IOException
     */
    @Test
    public void testWritingManyArraysInFile() throws IOException {
        final String fileName = "multi.mat";

        //test column-packed vector
        double[] src = new double[] { 1.3, 2.0, 3.0, 4.0, 5.0, 6.0 };
        double[] src2 = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
        double[] src3 = new double[] { 3.1415 };

        //create 3x2 double matrix
        //[ 1.0 4.0 ;
        //  2.0 5.0 ;
        //  3.0 6.0 ]
        MLDouble m1 = new MLDouble( "m1", src, 3 );
        MLDouble m2= new MLDouble( "m2", src2, 3 );
        MLDouble m3 = new MLDouble( "m3", src3, 1 );
        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( m1);
        list.add( m2);
        list.add( m3);

        //write arrays to file
        new MatFileWriter( fileName, list );

        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );

        //test if MLArray objects are equal
        assertEquals("Test if value red from file equals value stored", m1, mfr.getMLArray( "m1" ));
        assertEquals("Test if value red from file equals value stored", m2, mfr.getMLArray( "m2" ));
        assertEquals("Test if value red from file equals value stored", m3, mfr.getMLArray( "m3" ));
    }


    /**
     * Regression bug: Test writing several arrays into a single file.
     *
     * @throws IOException
     */
    @Test
    public void testIncrementalWrite() throws IOException {
        final String fileName = "multi.mat";

        //test column-packed vector
        double[] src = new double[] { 1.3, 2.0, 3.0, 4.0, 5.0, 6.0 };
        double[] src2 = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
        double[] src3 = new double[] { 3.1415 };

        //create 3x2 double matrix
        //[ 1.0 4.0 ;
        //  2.0 5.0 ;
        //  3.0 6.0 ]
        MLDouble m1 = new MLDouble( "m1", src, 3 );
        MLDouble m2= new MLDouble( "m2", src2, 3 );
        MLDouble m3 = new MLDouble( "m3", src3, 1 );
        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( m1);
        list.add( m2);
        list.add( m3);

        //write arrays to file
        MatFileIncrementalWriter writer = new MatFileIncrementalWriter( fileName );
        writer.write(m1);
        writer.write(m2);
        writer.write(m3);
        writer.close();

        //read array from file
        MatFileReader mfr = new MatFileReader( fileName );

        //test if MLArray objects are equal
        assertEquals("Test if value red from file equals value stored", m1, mfr.getMLArray( "m1" ));
        assertEquals("Test if value red from file equals value stored", m2, mfr.getMLArray( "m2" ));
        assertEquals("Test if value red from file equals value stored", m3, mfr.getMLArray( "m3" ));
    }

    /**
     *
     * <pre><code>
     * >> x = NaN;
     * >> save('nan', 'x');
     * </code></pre>
     * @throws IOException
     */
    @Test
    public void testReadingNaN() throws IOException {
        final String fileName = "test/nan.mat";

        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );

        assertEquals("Test if value red from file equals NaN", (Double) Double.NaN,
                                    (Double)((MLDouble)mfr.getMLArray( "x" )).get(0,0) );
    }

    @Test
    public void testWritingMethods() throws IOException
    {
        final String fileName = "nwrite.mat";
        final File f = new File(fileName);
        //test column-packed vector
        double[] src = new double[] { 1.3, 2.0, 3.0, 4.0, 5.0, 6.0 };

        //create 3x2 double matrix
        //[ 1.0 4.0 ;
        //  2.0 5.0 ;
        //  3.0 6.0 ]
        MLDouble m1 = new MLDouble( "m1", src, 3 );
        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( m1);

        MatFileWriter writer = new MatFileWriter();

        writer.write(f, list);

        assertTrue("Test if file was created", f.exists() );

        MLArray array = null;

        //try to read it
        MatFileReader reader = new MatFileReader();
        reader.read(f, MatFileReader.MEMORY_MAPPED_FILE );
        array = reader.getMLArray("m1");
        assertEquals("Test if is correct file", array, m1);

        //try to delete the file
        assertTrue("Test if file can be deleted", f.delete() );

        writer.write(fileName, list);

        assertTrue("Test if file was created", f.exists() );
        reader.read(f, MatFileReader.MEMORY_MAPPED_FILE );
        assertEquals("Test if is correct file", reader.getMLArray("m1"), m1);


        //try the same with direct buffer allocation
        reader.read(f, MatFileReader.DIRECT_BYTE_BUFFER );
        array = reader.getMLArray("m1");
        assertEquals("Test if is correct file", array, m1);

        //try to delete the file
        assertTrue("Test if file can be deleted", f.delete() );

        writer.write(fileName, list);

        assertTrue("Test if file was created", f.exists() );
        reader.read(f, MatFileReader.DIRECT_BYTE_BUFFER );
        assertEquals("Test if is correct file", reader.getMLArray("m1"), m1);

        //try the same with direct buffer allocation
        reader.read(f, MatFileReader.HEAP_BYTE_BUFFER);
        array = reader.getMLArray("m1");
        assertEquals("Test if is correct file", array, m1);

        //try to delete the file
        assertTrue("Test if file can be deleted", f.delete() );

        writer.write(fileName, list);

        assertTrue("Test if file was created", f.exists() );
        reader.read(f, MatFileReader.HEAP_BYTE_BUFFER );
        assertEquals("Test if is correct file", reader.getMLArray("m1"), m1);

    }

    /**
     * Test case that exposes the bug found by Julien C. from polymtl.ca
     * <p>
     * The test file contains a sparse array on crashes the reader. The bug
     * appeared when the {@link MLSparse} tried to allocate resources (very very
     * big {@link ByteBuffer}) and {@link IllegalArgumentException} was thrown.
     *
     * @throws IOException
     */
    @Test
    public void testBigSparseFile() throws IOException
    {
//        final File file = TestData.file(this,"bigsparse.mat");
//        //read array form file
//        MatFileReader mfr = new MatFileReader();
//        //reader crashes on reading this file
//        //bug caused by sparse array allocation
//        mfr.read( file, MatFileReader.DIRECT_BYTE_BUFFER );

    }

    @Test
    public void testMLCharStringArray()
    {
        String[] expected = new String[] { "a", "quick", "brown", "fox" };

        MLChar mlchar = new MLChar( "array", expected );

        assertEquals( expected[0], mlchar.getString(0) );
        assertEquals( expected[1], mlchar.getString(1) );
        assertEquals( expected[2], mlchar.getString(2) );
        assertEquals( expected[3], mlchar.getString(3) );
    }
}
