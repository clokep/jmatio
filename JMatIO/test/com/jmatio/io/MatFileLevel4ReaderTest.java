package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.jmatio.io.MatFileLevel5Reader;
import com.jmatio.io.MatlabIOException;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

/**
 * The test suite for MatFileLevel5Reader.
 *
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MatFileLevel4ReaderTest {
    @Test(expected = MatlabIOException.class)
    public void testReadingMethods() throws Exception {
        final String fileName = "test/double-v4.mat";

        // Try to read it.
        MatFileLevel5Reader reader = new MatFileLevel5Reader(fileName);
        MLArray array = reader.getMLArray("arr");
        //assertEquals("Test if is correct file", array, m1);
    }
}
