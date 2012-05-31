package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLUInt8;

/**
 * The test suite for MLUInt8.
 *
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLUInt8Test {
    @Test
    public void testObject() throws Exception {
        byte[] bytes = new byte[] {0, 1, 2, 3};
        MLUInt8 array = new MLUInt8("arr", bytes , 1);

        assertEquals(bytes[0], (byte)array.get(0));
        assertEquals(bytes[1], (byte)array.get(1));
        assertEquals(bytes[2], (byte)array.get(2));
        assertEquals(bytes[3], (byte)array.get(3));
    }

    @Test
    public void testExtremes() throws Exception {
        byte[] bytes = new byte[] {0, (byte)255};
        MLUInt8 array = new MLUInt8("arr", bytes , 1);

        assertEquals(bytes[0], (byte)array.get(0));
        assertEquals(bytes[1], (byte)array.get(1));
    }

    @Test
    public void testReadingNative() throws Exception {
        MLUInt8 expected = new MLUInt8("arr", new byte[] {0, (byte)255}, 1);

        // Test reading the MLArray generated natively by Matlab.
        MatFileReader reader = new MatFileReader();
        MLUInt8 readArray = (MLUInt8)reader.read(new File("test/uint8.mat")).get("arr");

        assertEquals(expected, readArray);
    }

    @Test
    public void testReadingAndWriting() throws Exception {
        byte[] bytes = new byte[] {0, 1, 2, 3};
        MLUInt8 array = new MLUInt8("arr", bytes , 1);

        // Test writing the MLUInt8.
        MatFileWriter writer = new MatFileWriter();
        writer.write("uint8tmp.mat", Arrays.asList((MLArray)array));

        // Test reading the MLUInt8.
        MatFileReader reader = new MatFileReader();
        MLUInt8 readArray = (MLUInt8)reader.read(new File("uint8tmp.mat")).get("arr");

        assertEquals(array, readArray);
    }

    /**
     * Tests <code>MLUint8</code> reading and writing.
     *
     * @throws IOException
     */
    @Test
    public void testMLUInt8Array() throws IOException {
        // Array name
        String name = "arr";
        // File name in which array will be stored.
        String fileName = "uint8tmp.mat";

        // Test column-packed vector
        byte[] src = new byte[] {1, 2, 3, 4, 5, 6};
        // Create 3x2 uint8 matrix
        // [1, 4;
        //  2, 5;
        //  3, 6]
        MLUInt8 mluint8 = new MLUInt8(name, src, 3);

        // Test 2D array coresponding to test vector
        byte[][] src2D = new byte[][]{{1, 4},
                                      {2, 5},
                                      {3, 6}};
        // Test 2D constructor.
        MLArray mluint82D = new MLUInt8(name, src2D);

        // Compare it with the original.
        assertEquals("Test if byte[][] constructor produces the same matrix as byte[].", mluint8, mluint82D);

        // Write array to file.
        new MatFileWriter(fileName, Arrays.asList((MLArray)mluint8));

        // Read array from file.
        MatFileReader mfr = new MatFileReader(fileName);
        MLUInt8 mlArrayRetrived = (MLUInt8)mfr.getMLArray(name);

        // Test if MLArray objects are equal
        assertEquals("Test if value red from file equals value stored", mluint8, mlArrayRetrived);

        // Test if 2D array match
        for (int i = 0; i < src2D.length; ++i) {
            boolean result = Arrays.equals(src2D[i], mlArrayRetrived.getArray()[i]);
            assertEquals("2D array match", true, result);
        }
    }

    @Test
    public void testUInt8() throws Exception {
        String fileName = "test/uint8.mat";
        String arrName = "arr";

        // Read array from file.
        MatFileReader mfr = new MatFileReader(fileName);
        MLUInt8 src = (MLUInt8)mfr.getMLArray(arrName);
        assertEquals("Test min. value from file: " + fileName + " array: " + arrName + ".",
                     (byte)0, (byte)src.get(0));
        assertEquals("Test max. value from file: " + fileName + " array: " + arrName + ".",
                     (byte)255, (byte)src.get(1));

        // Write.
        fileName = "uint8tmp.mat";
        new MatFileWriter(fileName, Arrays.asList((MLArray)src));

        // Read again.
        mfr = new MatFileReader(fileName);
        MLUInt8 ret = (MLUInt8)mfr.getMLArray(arrName);
        assertEquals("Test min. value from file: " + fileName + " array: " + arrName + ".",
                     (byte)0, (byte)ret.get(0));
        assertEquals("Test max. value from file: " + fileName + " array: " + arrName + ".",
                     (byte)255, (byte)ret.get(1));

        assertEquals("Test if array retrieved from " + fileName + " equals source array.",
                     src, ret);
    }
}
