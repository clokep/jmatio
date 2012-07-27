package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatlabIOException;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

/**
 * The test suite for MatFileReader.
 *
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MatFileLevel4ReaderTest {
    @Test(expected = MatlabIOException.class)
    public void testReadingMethods() throws Exception {
        final String fileName = "test/double-v4.mat";

        // Try to read it.
        MatFileReader reader = new MatFileReader(fileName);
        MLArray array = reader.getMLArray("arr");
        //assertEquals("Test if is correct file", array, m1);
    }
}
