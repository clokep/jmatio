package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.jmatio.io.MatFileLevel5Reader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

/**
 * The test suite for MatFileLevel5Reader.
 *
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MatFileLevel5ReaderTest {
    @Test
    public void testReadingMethods() throws Exception {
        final String fileName = "nwrite.mat";
        final File f = new File(fileName);
        // Test column-packed vector.
        double[] src = new double[] {1.3, 2.0, 3.0, 4.0, 5.0, 6.0};

        //create 3x2 double matrix
        //[ 1.0 4.0 ;
        //  2.0 5.0 ;
        //  3.0 6.0 ]
        MLDouble m1 = new MLDouble("m1", src, 3);

        // Write array to file.
        List<MLArray> list = Arrays.asList((MLArray)m1);
        MatFileWriter writer = new MatFileWriter();
        writer.write(f, list);

        assertTrue("Test if file was created", f.exists());

        MLArray array = null;

        // Try to read it.
        MatFileLevel5Reader reader = new MatFileLevel5Reader();
        reader.read(f, MatFileLevel5Reader.MEMORY_MAPPED_FILE);
        array = reader.getMLArray("m1");
        assertEquals("Test if is correct file", array, m1);

        // Try to delete the file.
        assertTrue("Test if file can be deleted", f.delete());

        // Rewrite the array.
        writer.write(fileName, list);

        assertTrue("Test if file was created", f.exists());
        reader.read(f, MatFileLevel5Reader.MEMORY_MAPPED_FILE);
        assertEquals("Test if is correct file", reader.getMLArray("m1"), m1);

        // Try the same with direct buffer allocation.
        reader.read(f, MatFileLevel5Reader.DIRECT_BYTE_BUFFER);
        array = reader.getMLArray("m1");
        assertEquals("Test if is correct file", array, m1);

        // Try to delete the file.
        assertTrue("Test if file can be deleted", f.delete());

        writer.write(fileName, list);

        assertTrue("Test if file was created", f.exists());
        reader.read(f, MatFileLevel5Reader.DIRECT_BYTE_BUFFER);
        assertEquals("Test if is correct file", reader.getMLArray("m1"), m1);

        // Try the same with heap buffer allocation.
        reader.read(f, MatFileLevel5Reader.HEAP_BYTE_BUFFER);
        array = reader.getMLArray("m1");
        assertEquals("Test if is correct file", array, m1);

        // Try to delete the file.
        assertTrue("Test if file can be deleted", f.delete());

        writer.write(fileName, list);

        assertTrue("Test if file was created", f.exists());
        reader.read(f, MatFileLevel5Reader.HEAP_BYTE_BUFFER);
        assertEquals("Test if is correct file", reader.getMLArray("m1"), m1);
    }
}
