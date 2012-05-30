package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;

/**
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLOpaqueTest {
    @Test
    public void testInt8() throws Exception {
        // Test reading the MLArray generated natively by Matlab.
        MatFileReader reader = new MatFileReader("test/object.mat");
        //MatFileReader reader = new MatFileReader("test/opaque.mat");
        //MatFileReader reader = new MatFileReader("test/java.lang.Integer.mat");
        MLArray readArray = reader.getContent().get("arr");
    }
}
