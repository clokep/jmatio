package com.jmatio.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.jmatio.io.MatFileLevel5Reader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.io.MatlabIOException;
import com.jmatio.types.MLArray;

/**
 * @author Patrick Cloke <pcloke@mitre.org>
 */
public class MLFunctionHandleTest {
    @Test(expected = MatlabIOException.class)
    public void testMLFunctionHandle() throws Exception {
        // Test reading the MLArray generated natively by Matlab.
        //MatFileLevel5Reader reader = new MatFileLevel5Reader("test/function_handle_anonymous.mat");
        MatFileLevel5Reader reader = new MatFileLevel5Reader("test/function_handle_builtin.mat");
        MLArray readArray = reader.getContent().get("arr");
    }
}
