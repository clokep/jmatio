package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;

/**
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MLCharTest {
    /**
     * Tests <code>MLChar</code> reading and writing.
     *
     * @throws IOException
     */
    @Test
    public void testMLCharArray() throws Exception {
        // Array name.
        String name = "chararr";
        // File name in which array will be stored.
        String fileName = "char-tmp.mat";
        String value = "dummy";

        // Create MLChar array of a name "chararr" containing one string value
        // "dummy".
        MLChar src = new MLChar(name, value);

        // Get array name.
        assertEquals("MLChar name getter", name, src.getName());

        // Get value of the first element.
        assertEquals("MLChar value getter", value, src.getString(0));

        //write arrays to file
        new MatFileWriter(fileName, Arrays.asList((MLArray)src));

        //read array form file
        MatFileReader mfr = new MatFileReader(fileName);
        MLArray ret = mfr.getMLArray(name);

        assertEquals("Test if value read from file equals value stored.", src, ret);

        //try to read non existent array
        ret = mfr.getMLArray("nonexistent");
        assertEquals("Test if non existent value is null", null, ret);
    }

    @Test
    public void testMLCharStringArray() {
        String[] expected = new String[]{"a", "quick", "brown", "fox"};

        MLChar mlchar = new MLChar("array", expected);

        assertEquals(expected[0], mlchar.getString(0));
        assertEquals(expected[1], mlchar.getString(1));
        assertEquals(expected[2], mlchar.getString(2));
        assertEquals(expected[3], mlchar.getString(3));
    }
}
