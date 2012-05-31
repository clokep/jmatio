package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLInt8;

/**
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MLInt8Test {
    @Test
    public void testInt8() throws Exception {
        String fileName = "test/int8.mat";
        String arrName = "arr";

        // Read the Matlab created array from file.
        MatFileReader mfr = new MatFileReader(fileName);
        MLInt8 src = (MLInt8)mfr.getMLArray("arr");

        // Check the values are as expected.
        assertEquals("Test min. value from file: " + fileName + " array: " + arrName + ".",
                     (byte)-128, (byte)src.get(0));
        assertEquals("Test max. value from file: " + fileName + " array: " + arrName + ".",
                     (byte)127, (byte)src.get(1));

        // Write the array back out.
        fileName = "int8tmp.mat";
        new MatFileWriter(fileName, Arrays.asList((MLArray)src));

        // Read the written array back in again.
        mfr = new MatFileReader(fileName);
        MLInt8 ret = (MLInt8)mfr.getMLArray(arrName);
        assertEquals("Test min. value from file: " + fileName + " array: " + arrName + ".",
                     (byte)-128, (byte)ret.get(0));
        assertEquals("Test max. value from file: " + fileName + " array: " + arrName + ".",
                     (byte)127, (byte)ret.get(1));

        assertEquals("Test that array retrieved from " + fileName + " equals source array.",
                     src, ret);
    }
}
