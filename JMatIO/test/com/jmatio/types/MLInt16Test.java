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

/**
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MLInt16Test {
    @Test
    public void testInt16() throws Exception {
        String fileName = "test/int16.mat";
        String arrName = "arr";
        MatFileReader mfr;

        // Read array from file.
        mfr = new MatFileReader(fileName);
        MLInt16 src = (MLInt16)mfr.getMLArray(arrName);

        assertEquals("Test min. value from file:" + fileName + " array: " + arrName,
                     (short)-32768, (short)src.get(0, 0));

        assertEquals("Test max. value from file:" + fileName + " array: " + arrName,
                     (short)32767, (short)src.get(0, 1));

        // Write.
        fileName = "int16tmp.mat";
        new MatFileWriter(fileName, Arrays.asList((MLArray)src));

        // Read again.
        mfr = new MatFileReader(fileName);
        assertEquals("Test min. value from file:" + fileName + " array: " + arrName,
                     (short)-32768,
                     (short)((MLInt16)mfr.getMLArray( arrName )).get(0,0) );

        assertEquals("Test max. value from file:" + fileName + " array: " + arrName,
                (short)32767,
                (short)((MLInt16)mfr.getMLArray( arrName )).get(0,1) );


        assertEquals("Test if array retrieved from " + fileName + " equals source array",
                src,
                mfr.getMLArray( arrName ) );
    }
}
