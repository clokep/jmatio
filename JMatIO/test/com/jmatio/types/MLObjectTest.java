package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLStructure;

/**
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLObjectTest {
    @Test
    public void testObjectFromMatlabCreatedFile() throws IOException {
        //array name
        File file = new File("test/inline.mat");
        MatFileReader reader = new MatFileReader(file);
        MLArray mlArray = reader.getMLArray("arr");

        List<MLArray> towrite =  Arrays.asList(mlArray);

        new MatFileWriter("inlinetmp.mat", towrite);

        reader = new MatFileReader("inlinetmp.mat");
        MLArray mlArrayRetrieved = reader.getMLArray("arr");

        assertEquals(mlArray, mlArrayRetrieved);
    }

}
