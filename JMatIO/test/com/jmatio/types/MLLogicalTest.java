package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.jmatio.io.MatFileLevel5Reader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLInt16;
import com.jmatio.types.MLLogical;

/**
 * The test suite for MLLogical.
 *
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MLLogicalTest {
    @Test
    public void testLogical() throws Exception {
        // The test file created in MATLAB that contains an array called "arr"
        // which contains the minimum and maximum values for logical.
        String fileName = "test/logical.mat";
        String arrName = "arr";
        MatFileLevel5Reader mfr;

        Boolean min = false;
        Boolean max = true;

        // Read array from file.
        mfr = new MatFileLevel5Reader(fileName);
        MLLogical src = (MLLogical)mfr.getMLArray(arrName);
        assertEquals("Test min. value from file: " + fileName + " array: " + arrName + ".",
                     min, src.get(0, 0));
        assertEquals("Test max. value from file:" + fileName + " array: " + arrName + ".",
                     max, src.get(0, 1));

        // Write the array out to a file.
        fileName = "logicaltmp.mat";
        new MatFileWriter(fileName, Arrays.asList((MLArray)src));

        // Read the array in again.
        mfr = new MatFileLevel5Reader(fileName);
        MLLogical dst = (MLLogical)mfr.getMLArray(arrName);
        assertEquals("Test min. value from file: " + fileName + " array: " + arrName + ".",
                     min, dst.get(0, 0));
        assertEquals("Test max. value from file:" + fileName + " array: " + arrName + ".",
                     max, dst.get(0, 1));

        assertEquals("Test if array retrieved from " + fileName + " equals source array.",
                     src, dst);
    }

    /**
     * Test a logical array that is not a mxINT8_CLASS array.
     */
    @Test
    public void testReadingAndWritingNonUInt8() throws Exception {
        MLInt16 array = new MLInt16("arr", new int[]{1, 5});
        array.set(new short[]{-32768, -1, 0, 1, 32767});
        MLLogical logical = new MLLogical(array);

        // Test writing the MLLogical.
        MatFileWriter writer = new MatFileWriter();
        String fileName = "logical-int16-tmp.mat";
        writer.write(fileName, Arrays.asList((MLArray)logical));

        // Test reading the MLLogical.
        MatFileLevel5Reader reader = new MatFileLevel5Reader();
        MLLogical readLogical = (MLLogical)reader.read(new File(fileName)).get("arr");

        assertEquals(logical, readLogical);
    }

    /**
     * Test whether the constuctor can handle an array with a length != a
     * multiple of dsize BufferOverflowException was thrown before a fix in
     * MLNumericArray.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMLLogicalConstructor(){
        int dsize = 2;
        Boolean data[] = new Boolean[]{true};

        new MLLogical("name", data, dsize);
    }
}
