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
import com.jmatio.types.MLSingle;

/**
 * Tests the mxSINGLE
 *
 * Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MLSingleTest {
    private final Float[] expected = new Float[] {1.1f, 2.2f, 3.3f};
    private final String name = "arr";
    private final MLSingle array = new MLSingle(this.name, this.expected , 1);

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(MLSingleTest.class);
    }

    @Test
    public void testObject() throws Exception {
        assertEquals(this.expected[0], this.array.get(0));
        assertEquals(this.expected[1], this.array.get(1));
        assertEquals(this.expected[2], this.array.get(2));
    }

    @Test
    public void testReadingNative() throws Exception {
        // Test reading the MLLogical generated natively by Matlab.
        MatFileReader reader = new MatFileReader();
        MLSingle readArray = (MLSingle)reader.read(new File("test/single.mat")).get("arr");

        assertEquals(this.array, readArray);
    }

    @Test
    public void testReadingAndWriting() throws Exception {
        // Test writing the MLLogical.
        MatFileWriter writer = new MatFileWriter();
        writer.write("singletmp.mat", Arrays.asList((MLArray)this.array));

        // Test reading the MLLogical.
        MatFileReader reader = new MatFileReader();
        MLSingle readArray = (MLSingle)reader.read(new File("singletmp.mat")).get("arr");

        assertEquals(this.array, readArray);
    }
}
