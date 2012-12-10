package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.jmatio.io.MatFileLevel5Reader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;

/**
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MLCellTest {
    /**
     * Test <code>MatFileFilter</code> options
     * @throws IOException
     */
    @Test
    public void testMLCell() throws IOException {
        // Array names.
        String name = "doublearr";
        String name2 = "name";
        // File name in which array will be stored.
        String fileName = "mlcell.mat";

        // Test column-packed vector.
        double[] src = new double[] {1.3, 2.0, 3.0, 4.0, 5.0, 6.0};

        // Create 3x2 double matrix.
        // [1.0 4.0;
        //  2.0 5.0;
        //  3.0 6.0]
        MLDouble mlDouble = new MLDouble(name, src, 3);
        MLChar mlChar = new MLChar(name2, "none");

        name = "cl";
        MLCell mlCell = new MLCell(name, new int[] {2,1});
        mlCell.set(mlChar, 0);
        mlCell.set(mlDouble, 1);

        // Write array to file.
        new MatFileWriter(fileName, Arrays.asList((MLArray)mlCell));

        // Read array from file.
        MatFileLevel5Reader mfr = new MatFileLevel5Reader(fileName);
        MLCell mlArrayRetrived = (MLCell)mfr.getMLArray(name);

        assertEquals(mlDouble, mlArrayRetrived.get(1));
        assertEquals(mlChar, mlArrayRetrived.get(0));
    }
}
