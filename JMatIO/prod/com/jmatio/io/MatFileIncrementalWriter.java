package com.jmatio.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import com.jmatio.common.MatDataTypes;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLNumericArray;
import com.jmatio.types.MLSparse;
import com.jmatio.types.MLStructure;

/**
 * MAT-file Incremental writer.
 *
 * An updated writer which allows adding variables incrementally
 * for the life of the writer.  This is necessary to allow large
 * variables to be written without having to hold onto then longer
 * than is necessary.
 *
 * The writer internally maintains a list of the variable names
 * it has written so far, and will throw an exception if the same
 * variable name is submitted more than once to the same reader.
 *
 * Usage:
 * <pre><code>
 * //1. First create example arrays
 * double[] src = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
 * MLDouble mlDouble = new MLDouble( "double_arr", src, 3 );
 * MLChar mlChar = new MLChar( "char_arr", "I am dummy" );
 *
 * //2. write arrays to file
 * MatFileIncrementalWriter writer = new MatFileIncrementalWriter( new File("mat_file.mat"));
 * writer.write(mlDouble);
 * writer.write(mlChar);
 *
 * writer.close();
 *
 * </code></pre>
 *
 * this is "equal" to Matlab commands:
 * <pre><code>
 * >> double_arr = [ 1 2; 3 4; 5 6];
 * >> char_arr = 'I am dummy';
 * >>
 * >> save('mat_file.mat', 'double_arr');
 * >> save('mat_file.mat', 'char_arr', '-append');
 * </pre></code>
 *
 * @author tkutz
 */
public class MatFileIncrementalWriter extends MatFileWriter {
    private boolean headerWritten = false;
    private boolean isStillValid = false;
    private Set<String> varNames = new TreeSet<String>();
    /**
     * Creates a writer to a file given the filename.
     *
     * @param fileName name of output file
     * @throws IOException
     * @throws DataFormatException
     */
    public MatFileIncrementalWriter(String fileName) throws IOException {
        this(new File(fileName));
    }
    /**
     * Creates a writer to a file given the File object.
     *
     * @param file an output <code>File</code>
     * @throws IOException
     * @throws DataFormatException
     */
    public MatFileIncrementalWriter(File file) throws IOException {
        this.fos = new FileOutputStream(file);
        this.isStillValid = true;
    }

    public synchronized void write(MLArray data) throws IOException {
        String vName = data.getName();
        if (this.varNames.contains(vName))
            throw new IllegalArgumentException("Error: variable " + vName + " specified more than once for file input.");
        try {
            // Write the header, but only once.
            if (!this.headerWritten) {
                this.writeHeader();
                this.headerWritten = true;
            }

            // Prepare buffer for MATRIX data.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream( baos );
            // Write MATRIX bytes into buffer.
            data.writeMatrix(dos);

            // Compress data to save storage.
            Deflater compresser = new Deflater();

            byte[] input = baos.toByteArray();

            ByteArrayOutputStream compressed = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(new DeflaterOutputStream(compressed, compresser));

            dout.write(input);

            dout.close();
            compressed.close();

            // Write COMPRESSED tag and compressed data into output channel.
            byte[] compressedBytes = compressed.toByteArray();
            OSArrayTag tag = new OSArrayTag(MatDataTypes.miCOMPRESSED, compressedBytes);
            tag.writeTo(this.fos);
        } catch (IOException e) {
            throw e;
        } finally { }
    }

    /**
     * Writes <code>MLArrays</code> into <code>WritableByteChannel</code>.
     *
     * @param data
     *            the collection of <code>{@link MLArray}</code> objects
     * @throws IOException
     *             if writing fails
     */
    public synchronized void write(Collection<MLArray> data) throws IOException {
        try {
            // Write data.
            for (MLArray matrix : data)
                this.write(matrix);
        } catch (IllegalArgumentException iae) {
            this.isStillValid = false;
            throw iae;
        } catch (IOException e) {
            throw e;
        }
    }

    public synchronized void close() throws IOException {
        this.fos.close();
    }
}
