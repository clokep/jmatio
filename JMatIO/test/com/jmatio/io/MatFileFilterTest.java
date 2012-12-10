package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.jmatio.io.MatFileFilter;
import com.jmatio.io.MatFileLevel5Reader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;

/**
 * The test suite for MatFileFilter.
 *
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MatFileFilterTest {
    /**
     * Tests filtered reading.
     *
     * @throws IOException
     */
    @Test
    public void testFilteredReading() throws IOException {
        // Create the arrays.
        // Array names.
        String name = "doublearr";
        String name2 = "dummy";
        // File name in which array will be stored.
        String fileName = "filter.mat";

        double[] src = new double[] {1.3, 2.0, 3.0, 4.0, 5.0, 6.0};
        MLDouble mlDouble = new MLDouble(name, src, 3);
        MLChar mlChar = new MLChar(name2, "I am dummy");

        // Write the arrays to file.
        new MatFileWriter(fileName, Arrays.asList((MLArray)mlDouble, (MLArray)mlChar));

        // Create new filter instance.
        MatFileFilter filter = new MatFileFilter();
        filter.addArrayName(name);

        // Read array from file.
        MatFileLevel5Reader mfr = new MatFileLevel5Reader(fileName, filter);

        // Check size of content.
        Map<String, MLArray> content = mfr.getContent();
        assertEquals("Test if only one array was read.", 1, content.size());
    }

    /**
     * Test <code>MatFileFilter</code> options.
     */
    @Test
    public void testMatFileFilter() {
        // Create new filter instance.
        MatFileFilter filter = new MatFileFilter();

        // Empty filter should match all patterns.
        assertEquals("Test if empty filter matches all patterns", true, filter.matches("any"));

        // Now add something to the filter.
        String name = "my_array";
        filter.addArrayName(name);

        // Test if filter matches my_array.
        assertTrue("Test if filter matches the given array name.", filter.matches(name));

        // Test if filter returns false if does not match given name.
        assertTrue("Test if filter does not match a non-existent name.", !filter.matches("dummy"));
    }
}
