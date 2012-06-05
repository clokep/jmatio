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
import com.jmatio.types.MLDouble;

/**
 * The test suite for MLDouble.
 *
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MLDoubleExtraTest {
    /**
     * Regression bug
     *
     * @throws Exception
     */
    @Test
    public void testDoubleFromMatlabCreatedFile() throws Exception {
        // Array name.
        String name = "arr";
        // File name in which array will be stored.
        String fileName = "test/double-regression1.mat";

        // Test column-packed vector.
        double[] data = new double[] {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
        MLDouble src = new MLDouble(name, data, 3);

        // Read array from file.
        MatFileReader mfr = new MatFileReader( fileName );
        MLArray ret = mfr.getMLArray( name );

        // Test if MLArray objects are equal.
        assertEquals("Test if value red from file equals value stored", src, ret);
    }

    /**
     * Regression bug.
     *
     * Matlab code:
     * <pre><code>
     * arr = [1.1, 4.4; 2.2, 5.5; 3.3, 6.6];
     * save('matnativedouble2', arr);
     * </code></pre>
     *
     * @throws Exception
     */
    @Test
    public void testDoubleFromMatlabCreatedFile2() throws Exception {
        // Array name.
        String name = "arr";
        // File name in which array will be stored.
        String fileName = "test/double-regression2.mat";

        // Test column-packed vector.
        double[] data = new double[] {1.1, 2.2, 3.3, 4.4, 5.5, 6.6};
        MLDouble src = new MLDouble(name, data, 3);

        // Read array from file.
        MatFileReader mfr = new MatFileReader(fileName);
        MLArray ret = mfr.getMLArray(name);

        // Test if MLArray objects are equal.
        assertEquals("Test if value red from file equals value stored", src, ret);
    }

    /**
     * MATLAB code:
     * <pre><code>
     * x = NaN;
     * save('double-nan', 'x');
     * </code></pre>
     *
     * @throws Exception
     */
    @Test
    public void testReadingNaN() throws Exception {
        final String fileName = "test/double-nan.mat";

        // Read array from file
        MatFileReader mfr = new MatFileReader(fileName);
        MLDouble x = (MLDouble)mfr.getMLArray("x");

        assertEquals("Test if value read from file equals NaN.",
                     (Double)Double.NaN, (Double)x.get(0, 0));
    }

    /**
     * Test whether the constuctor can handle an array with a length != a
     * multiple of dsize BufferOverflowException was thrown before a fix in
     * MLNumericArray.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMLDoubleConstructor(){
        int dsize = 2;
        Double data[] = new Double[]{1.0};

        new MLDouble("name", data, dsize);
    }
}
