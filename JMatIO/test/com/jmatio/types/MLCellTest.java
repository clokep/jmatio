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

import com.jmatio.io.MatFileReader;
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
        //array name
        String name = "doublearr";
        String name2 = "name";
        //file name in which array will be storred
        String fileName = "mlcell.mat";

        //test column-packed vector
        double[] src = new double[] { 1.3, 2.0, 3.0, 4.0, 5.0, 6.0 };

        //create 3x2 double matrix
        //[ 1.0 4.0 ;
        //  2.0 5.0 ;
        //  3.0 6.0 ]
        MLDouble mlDouble = new MLDouble( name, src, 3 );
        MLChar mlChar = new MLChar( name2, "none" );


        MLCell mlCell = new MLCell("cl", new int[] {2,1} );
        mlCell.set(mlChar, 0);
        mlCell.set(mlDouble, 1);

        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mlCell );

        //write arrays to file
        new MatFileWriter( fileName, list );

        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLCell mlArrayRetrived = (MLCell)mfr.getMLArray( "cl" );

        assertEquals(mlDouble, mlArrayRetrived.get(1) );
        assertEquals(mlChar, mlArrayRetrived.get(0) );
    }

    @Test
    public void testCellFromMatlabCreatedFile() throws IOException {
        //array name
        File file = new File("test/cell.mat");
        MatFileReader reader = new MatFileReader( file );
        MLArray mlArray = reader.getMLArray( "cel" );

        List<MLArray> towrite =  Arrays.asList( mlArray );

        MatFileWriter  writer = new MatFileWriter( "cellcopy.mat", towrite );

        reader = new MatFileReader("cellcopy.mat");
        MLArray mlArrayRetrieved = reader.getMLArray( "cel" );

        //assertEquals( ((MLCell)mlArray).get(0), ((MLCell)mlArrayRetrieved).get(0));
    }
}
