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
     * @throws Exception
     */
    @Test public void testMLSparse() throws Exception {
        // Array name.
        String name = "sparsearr";
        // File name in which array will be stored.
        String fileName = "sparse-tmp.mat";

        // Test 2D array coresponding to test vector.
        double[][] referenceReal = new double[][] {{1.3, 4.0},
                                                   {2.0, 0.0},
                                                   {0.0, 0.0}};
        double[][] referenceImaginary = new double[][] {{0.0, 0.0},
                                                        {2.0, 0.0},
                                                        {0.0, 6.0}};

        MLSparse src = new MLSparse(name, new int[] {3, 2}, MLArray.mtFLAG_COMPLEX, 5);
        src.setReal(1.3, 0, 0);
        src.setReal(4.0, 0, 1);
        src.setReal(2.0, 1, 0);
        src.setImaginary(2.0, 1, 0);
        src.setImaginary(6.0, 2, 1);

        // Write arrays to file.
        new MatFileWriter(fileName, Arrays.asList((MLArray)src));

        // Read array from file.
        MatFileReader mfr = new MatFileReader(fileName);
        MLSparse ret = (MLSparse)mfr.getMLArray(name);

        // Test if MLArray objects are equal.
        assertEquals("Test if value read from file equals value stored.", src, ret);

        // Test if 2D arrays match.
        for (int i = 0; i < referenceReal.length; ++i) {
            for (int j = 0; j < referenceReal[i].length; ++j)
                assertEquals("2D array mismatch (real).", (Double)referenceReal[i][j], ret.getReal(i, j));
        }
        for (int i = 0; i < referenceImaginary.length; ++i) {
            for (int j = 0; j < referenceImaginary[i].length; ++j)
                assertEquals("2D array mismatch (imaginary).", (Double)referenceImaginary[i][j], ret.getImaginary(i, j));
        }
    }

    @Test
    public void testSparseFromMatlabCreatedFile() throws IOException {
        // Array name
        File file = new File("test/sparse.mat");
        String name = "spa";
        MatFileReader reader = new MatFileReader(file);
        MLArray src = reader.getMLArray(name);

        String filename = "sparse-tmp.mat";
        new MatFileWriter(filename, Arrays.asList((MLArray)src));

        reader = new MatFileReader(filename);
        MLArray ret = reader.getMLArray(name);

        assertEquals(src, ret);
    }

    /**
     * Test case that exposes the bug found by Julien C. from polymtl.ca.
     * <p>
     * The test file contains a sparse array on crashes the reader. The bug
     * appeared when the {@link MLSparse} tried to allocate resources (very very
     * big {@link ByteBuffer}) and {@link IllegalArgumentException} was thrown.
     *
     * @throws Exception
     */
    @Test
    public void testBigSparseFile() throws Exception {
        final File file = new File("test/sparse-big.mat");
        // Read array form file.
        MatFileReader mfr = new MatFileReader();

        // Reader crashes on reading this file bug caused by sparse array
        // allocation.
        mfr.read(file, MatFileReader.DIRECT_BYTE_BUFFER);
    }
}
