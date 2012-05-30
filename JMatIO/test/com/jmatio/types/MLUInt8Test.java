package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(MLUInt8Test.class);
    }

    @Test
    public void testObject() throws Exception {
        byte[] bytes = new byte[] {0, 1, 2, 3};
        MLUInt8 array = new MLUInt8("arr", bytes , 1);

        assertEquals(bytes[0], (byte)array.get(0));
        assertEquals(bytes[1], (byte)array.get(1));
        assertEquals(bytes[2], (byte)array.get(2));
        assertEquals(bytes[3], (byte)array.get(3));
    }

    @Test
    public void testExtremes() throws Exception {
        byte[] bytes = new byte[] {0, (byte)255};
        MLUInt8 array = new MLUInt8("arr", bytes , 1);

        assertEquals(bytes[0], (byte)array.get(0));
        assertEquals(bytes[1], (byte)array.get(1));
    }

    @Test
    public void testReadingNative() throws Exception {
        MLUInt8 expected = new MLUInt8("arr", new byte[] {0, (byte)255}, 1);

        // Test reading the MLArray generated natively by Matlab.
        MatFileReader reader = new MatFileReader();
        MLUInt8 readArray = (MLUInt8)reader.read(new File("test/uint8.mat")).get("arr");

        assertEquals(expected, readArray);
    }

    @Test
    public void testReadingAndWriting() throws Exception {
        byte[] bytes = new byte[] {0, 1, 2, 3};
        MLUInt8 array = new MLUInt8("arr", bytes , 1);

        // Test writing the MLUInt8.
        MatFileWriter writer = new MatFileWriter();
        writer.write("uint8tmp.mat", Arrays.asList((MLArray)array));

        // Test reading the MLUInt8.
        MatFileReader reader = new MatFileReader();
        MLUInt8 readArray = (MLUInt8)reader.read(new File("uint8tmp.mat")).get("arr");

        assertEquals(array, readArray);
    }

    /**
     * Tests <code>MLUint8</code> reading and writing.
     *
     * @throws IOException
     */
    @Test
    public void testMLUInt8Array() throws IOException {
        // Array name
        String name = "arr";
        // File name in which array will be stored.
        String fileName = "mluint8tst.mat";

        //test column-packed vector
        byte[] src = new byte[] {1, 2, 3, 4, 5, 6};
        //test 2D array coresponding to test vector
        byte[][] src2D = new byte[][] {{ 1, 4 },
                                       { 2, 5 },
                                       { 3, 6 }};

        //create 3x2 double matrix
        // [1.0 4.0;
        //  2.0 5.0;
        //  3.0 6.0]
        MLUInt8 mluint8 = new MLUInt8(name, src, 3);

        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mluint8 );

        //write arrays to file
        new MatFileWriter(fileName, list);

        //read array form file
        MatFileReader mfr = new MatFileReader(fileName);
        MLArray mlArrayRetrived = mfr.getMLArray(name);

        //test if MLArray objects are equal
        assertEquals("Test if value red from file equals value stored", mluint8, mlArrayRetrived);

        //test if 2D array match
        for (int i = 0; i < src2D.length; ++i) {
            boolean result = Arrays.equals(src2D[i], ((MLUInt8)mlArrayRetrived).getArray()[i]);
            assertEquals("2D array match", true, result);
        }

        //test new constructor
        MLArray mlMLUInt82D = new MLUInt8(name, src2D);
        //compare it with original
        assertEquals("Test if double[][] constructor produces the same matrix as normal one", mlMLUInt82D, mluint8);
    }

    @Test
    public void testUInt8() throws Exception {
        String fileName = "test/uint8.mat";
        String arrName = "arr";
        MatFileReader mfr;
        MLArray src;

        //read array form file
        mfr = new MatFileReader( fileName );
        assertEquals("Test min. value from file:" + fileName + " array: " + arrName,
                     (byte)0,
                     (byte)((MLUInt8)mfr.getMLArray( arrName )).get(0,0) );

        assertEquals("Test max. value from file:" + fileName + " array: " + arrName,
                (byte)255,
                (byte)((MLUInt8)mfr.getMLArray( arrName )).get(0,1) );

        src = mfr.getMLArray( arrName );

        //write
        fileName = "uint8out.mat";
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add( mfr.getMLArray( arrName ) );
        new MatFileWriter(fileName, towrite );

        //read again
        mfr = new MatFileReader( fileName );
        assertEquals("Test min. value from file:" + fileName + " array: " + arrName,
                     (byte)0,
                     (byte)((MLUInt8)mfr.getMLArray( arrName )).get(0,0) );

        assertEquals("Test max. value from file:" + fileName + " array: " + arrName,
                (byte)255,
                (byte)((MLUInt8)mfr.getMLArray( arrName )).get(0,1) );


        assertEquals("Test if array retrieved from " + fileName + " equals source array",
                src,
                mfr.getMLArray( arrName ) );
    }
}
