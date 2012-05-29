package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLSparse;

/**
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MLSparseTest {
    /**
     * Tests <code>MLSparse</code> reading and writing.
     *
     * @throws IOException
     */
    @Test public void testMLSparse() throws IOException
    {
        //array name
        String name = "sparsearr";
        //file name in which array will be storred
        String fileName = "mlsparse.mat";

        //test 2D array coresponding to test vector
        double[][] referenceReal = new double[][] { { 1.3, 4.0 },
                { 2.0, 0.0 },
                { 0.0, 0.0 }
            };
        double[][] referenceImaginary = new double[][] { { 0.0, 0.0 },
                { 2.0, 0.0 },
                { 0.0, 6.0 }
            };

        MLSparse mlSparse = new MLSparse(name, new int[] {3, 2}, MLArray.mtFLAG_COMPLEX, 5);
        mlSparse.setReal(1.3, 0, 0);
        mlSparse.setReal(4.0, 0, 1);
        mlSparse.setReal(2.0, 1, 0);
        mlSparse.setImaginary(2.0, 1, 0);
        mlSparse.setImaginary(6.0, 2, 1);

        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mlSparse );

        //write arrays to file
        new MatFileWriter( fileName, list );

        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLArray mlArrayRetrived = mfr.getMLArray( name );

        //test if MLArray objects are equal
        assertEquals("Test if value red from file equals value stored", mlSparse, mlArrayRetrived);

        //test if 2D array match
        for ( int i = 0; i < referenceReal.length; i++ )
        {
            for (int j = 0; j < referenceReal[i].length; j++) {
                assertEquals( "2D array mismatch (real)", (Double)referenceReal[i][j], ((MLSparse)mlArrayRetrived).getReal(i,j));
            }
        }
        for ( int i = 0; i < referenceImaginary.length; i++ )
        {
            for (int j = 0; j < referenceImaginary[i].length; j++) {
                assertEquals( "2D array mismatch (imaginary)", (Double)referenceImaginary[i][j], ((MLSparse)mlArrayRetrived).getImaginary(i,j));
            }
        }
    }


    @Test
    public void testSparseFromMatlabCreatedFile() throws IOException {
        //array name
        File file = new File("test/sparse.mat");
        MatFileReader reader = new MatFileReader( file );
        MLArray mlArray = reader.getMLArray( "spa" );

        List<MLArray> towrite =  Arrays.asList( mlArray );

        new MatFileWriter( "sparsecopy.mat", towrite );

        reader = new MatFileReader("sparsecopy.mat");
        MLArray mlArrayRetrieved = reader.getMLArray( "spa" );

        assertEquals(mlArray, mlArrayRetrieved);
    }
}
