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
import com.jmatio.types.MLJavaObject;

/**
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLOpaqueTest {
    @Test
    public void testInt8() throws Exception {
        // Test reading the MLArray generated natively by Matlab.
        //String fileName = "test/containers.Map2.mat";
        String fileName = "test/opaque-containers.Map.mat";
        //String fileName = "../opaque-mixed.mat";
        MatFileReader reader = new MatFileReader(fileName);
        MLArray src = reader.getContent().get("arr");
        MLOpaque x = (MLOpaque)src;

        // Write the array out to a file.
        fileName = "opaque-containers.Map-tmp.mat";
        new MatFileWriter(fileName, Arrays.asList((MLArray)src));
    }

    @Test
    public void testOpaqueInteger() throws Exception {
        // Read the MATLAB created array.
        String fileName = "test/opaque-java.lang.Integer.mat";
        String arrName = "arr";
        MatFileReader reader = new MatFileReader(fileName);
        MLJavaObject src = (MLJavaObject)reader.getContent().get(arrName);

        // Check the contents are what is expected.
        Integer ret = (Integer)src.getObject();
        assertEquals(new Integer(10), ret);

        // Write the array out to a file.
        fileName = "opaque-java.lang.Integer-tmp.mat";
        new MatFileWriter(fileName, Arrays.asList((MLArray)src));

        // Read the array in again.
        reader = new MatFileReader(fileName);
        MLJavaObject dst = (MLJavaObject)reader.getMLArray(arrName);
        assertEquals("Test if array contents retrieved from " + fileName + " equals source array contents.",
                     src.getObject(), dst.getObject());
        assertEquals("Test if array retrieved from " + fileName + " equals source array.",
                     src, dst);
    }

    @Test
    public void testOpaqueIntegerArray() throws Exception {
        MatFileReader reader = new MatFileReader("test/opaque-java.lang.Integer-array.mat");
        MLJavaObject src = (MLJavaObject)reader.getContent().get("arr");

        Integer[] ret = (Integer[])src.getObject();
        Integer[] expected = new Integer[2];
        expected[0] = Integer.MIN_VALUE;
        expected[1] = Integer.MAX_VALUE;

        assertArrayEquals(expected, ret);
    }
}
