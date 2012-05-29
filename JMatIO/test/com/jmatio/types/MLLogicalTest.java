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
import com.jmatio.types.MLInt16;
import com.jmatio.types.MLLogical;

/**
 * The test suite for MLLogical.
 *
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLLogicalTest {
    private final boolean[] bools = new boolean[] {false, true};
    private final MLLogical array = new MLLogical("arr", this.bools , 1);

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(MLLogicalTest.class);
    }

	@Test
	public void testObject() throws Exception {
        assertEquals(this.bools[0], this.array.get(0));
        assertEquals(this.bools[1], this.array.get(1));
    }

    @Test
    public void testReadingNative() throws Exception {
        // Test reading the MLLogical generated natively by Matlab R2012a.
        MatFileReader reader = new MatFileReader();
        MLLogical readLogical = (MLLogical) reader.read(new File("test/logical.mat")).get("arr");

        assertEquals(this.array, readLogical);
	}

    @Test
    public void testReadingAndWriting() throws Exception {
        // Test writing the MLLogical.
        MatFileWriter writer = new MatFileWriter();
        writer.write("logicaltmp.mat", Arrays.asList((MLArray)this.array));

        // Test reading the MLLogical.
        MatFileReader reader = new MatFileReader();
        MLLogical readLogical = (MLLogical)reader.read(new File("logicaltmp.mat")).get("arr");

        assertEquals(this.array, readLogical);
    }

    @Test
    public void testReadingAndWritingNonUInt8() throws Exception {
        MLInt16 array = new MLInt16("arr", new int[]{1, 5}, MLArray.mxINT16_CLASS, MLArray.mtFLAG_LOGICAL);
        array.set(new short[]{-32768, -1, 0, 1, 32767});
        MLLogical logical = new MLLogical(array);

        // Test writing the MLLogical.
        MatFileWriter writer = new MatFileWriter();
        writer.write("logicaltmp2.mat", Arrays.asList((MLArray)logical));

        // Test reading the MLLogical.
        MatFileReader reader = new MatFileReader();
        MLLogical readLogical = (MLLogical)reader.read(new File("logicaltmp2.mat")).get("arr");

        assertEquals(logical, readLogical);
    }
}
