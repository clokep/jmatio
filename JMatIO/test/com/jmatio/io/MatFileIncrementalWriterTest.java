package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.jmatio.io.MatFileIncrementalWriter;
import com.jmatio.io.MatFileLevel5Reader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

/**
 * The test suite for MatFileIncrementalWriter.
 *
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MatFileIncrementalWriterTest {
    /**
     * Regression bug: Test writing several arrays into a single file.
     *
     * @throws IOException
     */
    @Test
    public void testIncrementalWrite() throws Exception {
        final String fileName = "multi.mat";

        // Test column-packed vector.
        double[] src = new double[] {1.3, 2.0, 3.0, 4.0, 5.0, 6.0};
        double[] src2 = new double[] {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
        double[] src3 = new double[] {3.14159};

        MLDouble m1 = new MLDouble("m1", src, 3);
        MLDouble m2 = new MLDouble("m2", src2, 3);
        MLDouble m3 = new MLDouble("m3", src3, 1);
        // Add arrays to a list.
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add(m1);
        list.add(m2);
        list.add(m3);

        // Write the arrays to file.
        MatFileIncrementalWriter writer = new MatFileIncrementalWriter(fileName );
        writer.write(m1);
        writer.write(m2);
        writer.write(m3);
        writer.close();

        //read array from file
        MatFileLevel5Reader mfr = new MatFileLevel5Reader(fileName);

        // Test if MLArray objects are equal.
        assertEquals("Test if value read from file equals value stored.", m1, mfr.getMLArray("m1"));
        assertEquals("Test if value read from file equals value stored.", m2, mfr.getMLArray("m2"));
        assertEquals("Test if value read from file equals value stored.", m3, mfr.getMLArray("m3"));
    }
}
