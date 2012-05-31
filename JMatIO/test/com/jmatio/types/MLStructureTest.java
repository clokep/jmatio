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
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MLStructureTest {
    /**
     * Test <code>MatFileFilter</code> options
     * @throws IOException
     */
    @Test
    public void testMLStructure() throws IOException {
        //array name
        //file name in which array will be storred
        String fileName = "mlstruct.mat";

        //test column-packed vector
        double[] src = new double[] { 1.3, 2.0, 3.0, 4.0, 5.0, 6.0 };

        //create 3x2 double matrix
        //[ 1.0 4.0 ;
        //  2.0 5.0 ;
        //  3.0 6.0 ]
        MLDouble mlDouble = new MLDouble( null, src, 3 );
        MLChar mlChar = new MLChar( null, "I am dummy" );


        MLStructure mlStruct = new MLStructure("str", new int[] {1,1} );
        mlStruct.setField("f1", mlDouble);
        mlStruct.setField("f2", mlChar);

        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mlStruct );

        //write arrays to file
        new MatFileWriter( fileName, list );

        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLStructure mlArrayRetrived = (MLStructure)mfr.getMLArray( "str" );

        assertEquals(mlDouble, mlArrayRetrived.getField("f1") );
        assertEquals(mlChar, mlArrayRetrived.getField("f2") );
    }

    @Test
    public void testMLStructureFieldNames() throws IOException {
        //test column-packed vector
        double[] src = new double[] { 1.3, 2.0, 3.0, 4.0, 5.0, 6.0 };

        //create 3x2 double matrix
        //[ 1.0 4.0 ;
        //  2.0 5.0 ;
        //  3.0 6.0 ]
        MLDouble mlDouble = new MLDouble( null, src, 3 );
        MLChar mlChar = new MLChar( null, "I am dummy" );


        MLStructure mlStruct = new MLStructure("str", new int[] {1,1} );
        mlStruct.setField("f1", mlDouble);
        mlStruct.setField("f2", mlChar);

        Collection<String> fieldNames = mlStruct.getFieldNames();

        assertEquals( 2, fieldNames.size() );
        assertTrue( fieldNames.contains("f1") );
        assertTrue( fieldNames.contains("f2") );
    }


    @Test
    public void testStructureFromMatlabCreatedFile() throws IOException {
        //array name
        File file = new File("test/simplestruct.mat");
        MatFileReader reader = new MatFileReader( file );
        MLArray mlArray = reader.getMLArray( "structure" );

        List<MLArray> towrite =  Arrays.asList( mlArray );

        new MatFileWriter( "simplestructcopy.mat", towrite );

        reader = new MatFileReader("simplestructcopy.mat");
        MLArray mlArrayRetrieved = reader.getMLArray( "structure" );

        assertEquals(mlArray, mlArrayRetrieved);
    }

}
