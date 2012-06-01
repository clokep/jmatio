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
import com.jmatio.types.MLInt64;

/**
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MLInt64Test {
    @Test
    public void testInt64() throws Exception {
        String fileName = "test/int64.mat";
        String arrName = "arr";

        Long max = Long.parseLong("9223372036854775807");
        Long min = Long.parseLong("-9223372036854775808");

        // Read array from file.
        MatFileReader mfr = new MatFileReader(fileName);
        MLInt64 src = (MLInt64)mfr.getMLArray(arrName);

        assertEquals("Test min. value from file: " + fileName + " array: " + arrName + ".",
                     min, src.get(0, 0));
        assertEquals("Test max. value from file: " + fileName + " array: " + arrName + ".",
                     max, src.get(0, 1));

        // Write.
        fileName = "int64tmp.mat";
        new MatFileWriter(fileName, Arrays.asList((MLArray)src));

        // Read again.
        mfr = new MatFileReader( fileName );
        MLInt64 dst = (MLInt64)mfr.getMLArray(arrName);
        assertEquals("Test min. value from file: " + fileName + " array: " + arrName + ".",
                     min, src.get(0, 0));
        assertEquals("Test max. value from file: " + fileName + " array: " + arrName + ".",
                     max, src.get(0, 1));

        assertEquals("Test if array retrieved from " + fileName + " equals source array.",
                     src, dst);
    }
}
