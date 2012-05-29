package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;

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
        MatFileReader mfr;
        MLArray src;

        Long max = Long.parseLong("9223372036854775807");
        Long min = Long.parseLong("-9223372036854775808");

        //read array form file
        mfr = new MatFileReader( fileName );

        assertEquals("Test min. value from file:" + fileName + " array: " + arrName,
                     min,
                     ((MLInt64)mfr.getMLArray( "arr" )).get(0,0) );

        assertEquals("Test max. value from file:" + fileName + " array: " + arrName,
                    max,
                    ((MLInt64)mfr.getMLArray( "arr" )).get(0,1) );

        src = mfr.getMLArray( "arr" );

        //write
        fileName = "int64out.mat";
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add( mfr.getMLArray( arrName ) );
        new MatFileWriter(fileName, towrite );

        //read again
        mfr = new MatFileReader( fileName );
        assertEquals("Test min. value from file:" + fileName + " array: " + arrName,
                     min,
                     ((MLInt64)mfr.getMLArray( arrName )).get(0,0) );

        assertEquals("Test max. value from file:" + fileName + " array: " + arrName,
                    max,
                    ((MLInt64)mfr.getMLArray( arrName )).get(0,1) );


        assertEquals("Test if array retrieved from " + fileName + " equals source array",
                src,
                mfr.getMLArray( arrName ) );
    }
}
