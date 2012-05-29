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
import com.jmatio.types.MLUInt8;

/**
 * The test suite for MLUInt8.
 *
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLUInt8Test {
    private final byte[] bytes = new byte[] {0, 1, 2, 3};
    private final MLUInt8 array = new MLUInt8("arr", this.bytes , 1);

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(MLUInt8Test.class);
    }

	@Test
	public void testObject() throws Exception {
        assertEquals(this.bytes[0], (byte)this.array.get(0));
        assertEquals(this.bytes[1], (byte)this.array.get(1));
        assertEquals(this.bytes[2], (byte)this.array.get(2));
        assertEquals(this.bytes[3], (byte)this.array.get(3));
    }

    @Test
    public void testReadingNative() throws Exception {
		MLUInt8 expected = new MLUInt8("arr", new byte[] {(byte)0, (byte)255}, 1);

        // Test reading the MLArray generated natively by Matlab.
        MatFileReader reader = new MatFileReader();
        MLUInt8 readArray = (MLUInt8)reader.read(new File("test/uint8.mat")).get("arr");

        assertEquals(expected, readArray);
	}

    @Test
    public void testReadingAndWriting() throws Exception {
        // Test writing the MLUInt8.
        MatFileWriter writer = new MatFileWriter();
        writer.write("uint8tmp.mat", Arrays.asList((MLArray)this.array));

        // Test reading the MLUInt8.
        MatFileReader reader = new MatFileReader();
        MLUInt8 readArray = (MLUInt8)reader.read(new File("uint8tmp.mat")).get("arr");

        assertEquals(this.array, readArray);
    }
}
