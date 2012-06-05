package com.jmatio.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import com.jmatio.types.MLArray;

/**
 * MAT-file incremental writer.
 *
 * An updated writer which allows adding variables incrementally for the life
 * of the writer. This is necessary to allow large variables to be written
 * without having to hold onto then longer than is necessary.
 *
 * The writer internally maintains a list of the variable names it has written
 * so far, and will throw an exception if the same variable name is submitted
 * more than once to the same reader.
 *
 * Usage:
 * <pre><code>
 * // 1. First create example arrays.
 * double[] src = new double[] {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
 * MLDouble mlDouble = new MLDouble( "double_arr", src, 3);
 * MLChar mlChar = new MLChar("char_arr", "I am dummy");
 *
 * // 2. Write arrays to file.
 * MatFileIncrementalWriter writer = new MatFileIncrementalWriter("mat_file.mat");
 * writer.write(mlDouble);
 * writer.write(mlChar);
 *
 * writer.close();
 * </code></pre>
 *
 * This is "equal" to Matlab commands:
 * <pre><code>
 * >> double_arr = [1 2; 3 4; 5 6];
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
            this.writeHeader();
            this.writeMatrix(data);
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

    @Override
    protected void writeHeader() throws IOException {
        // Write the header, but only once.
        if (this.headerWritten)
            return;

        super.writeHeader();
        this.headerWritten = true;
    }

    public synchronized void close() throws IOException {
        this.fos.close();
    }
}
