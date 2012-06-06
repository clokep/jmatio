package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLOpaque;

/**
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLOpaqueTest {
    @Test
    public void testInt8() throws Exception {
        // Test reading the MLArray generated natively by Matlab.
        MatFileReader reader = new MatFileReader("test/containers.Map.mat");
        //MatFileReader reader = new MatFileReader("test/containers.Map2.mat");
        MLArray readArray = reader.getContent().get("arr");
    }

    @Test
    public void testOpaqueInteger() throws Exception {
        // Read the MATLAB created array.
        String fileName = "test/opaque-java.lang.Integer.mat";
        String arrName = "arr";
        MatFileReader reader = new MatFileReader(fileName);
        MLOpaque src = (MLOpaque)reader.getContent().get(arrName);

        // Check the contents are what is expected.
        Integer ret = (Integer)src.get();
        assertEquals(new Integer(10), ret);

        // Write the array out to a file.
        fileName = "opaque-java.lang.Integer-tmp.mat";
        new MatFileWriter(fileName, Arrays.asList((MLArray)src));

        // Read the array in again.
        reader = new MatFileReader(fileName);
        MLOpaque dst = (MLOpaque)reader.getMLArray(arrName);
        assertEquals("Test if array contents retrieved from " + fileName + " equals source array contents.",
                     src.get(), dst.get());
        assertEquals("Test if array retrieved from " + fileName + " equals source array.",
                     src, dst);
    }

    @Test
    public void testOpaqueIntegerArray() throws Exception {
        MatFileReader reader = new MatFileReader("test/opaque-java.lang.Integer-array.mat");
        MLOpaque readArray = (MLOpaque)reader.getContent().get("arr");

        Integer[] ret = (Integer[])readArray.get();
        Integer[] expected = new Integer[2];
        expected[0] = Integer.MIN_VALUE;
        expected[1] = Integer.MAX_VALUE;

        assertArrayEquals(expected, ret);
    }
}
