package com.jmatio.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import com.jmatio.common.MatDataTypes;
import com.jmatio.extra.VariableUtils;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLNumericArray;
import com.jmatio.types.MLSparse;
import com.jmatio.types.MLStructure;

/**
 * MAT-file writer.
 *
 * Usage:
 * <pre><code>
 * //1. First create example arrays
 * double[] src = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
 * MLDouble mlDouble = new MLDouble( "double_arr", src, 3 );
 * MLChar mlChar = new MLChar( "char_arr", "I am dummy" );
 *
 * //2. write arrays to file
 * ArrayList<MLArray> list = new ArrayList<MLArray>();
 * list.add( mlDouble );
 * list.add( mlChar );
 *
 * new MatFileWriter( "mat_file.mat", list );
 * </code></pre>
 *
 * this is "equal" to Matlab commands:
 * <pre><code>
 * >> double_arr = [ 1 2; 3 4; 5 6];
 * >> char_arr = 'I am dummy';
 * >>
 * >> save('mat_file.mat', 'double_arr', 'char_arr');
 * </pre></code>
 *
 * @author Wojciech Gradkowski (<a href="mailto:wgradkowski@gmail.com">wgradkowski@gmail.com</a>)
 */
public class MatFileWriter {
    protected FileOutputStream fos;

    /**
     * Creates the new <code>{@link MatFileWriter}</code> instance
     */
    public MatFileWriter() {
        super();
    }

    /**
     * Writes MLArrays into file given by <code>fileName</code>.
     *
     * @param fileName name of ouput file
     * @param data <code>Collection</code> of <code>MLArray</code> elements
     * @throws IOException
     * @throws DataFormatException
     */
    public MatFileWriter(String fileName, Collection<MLArray> data) throws IOException {
        this(new File(fileName), data);
    }

    /**
     * Writes MLArrays into <code>File</code>.
     *
     * @param file an output <code>File</code>
     * @param data <code>Collection</code> of <code>MLArray</code> elements
     * @throws IOException
     * @throws DataFormatException
     */
    public MatFileWriter(File file, Collection<MLArray> data) throws IOException {
        this.write(file, data);
    }

    /**
     * Writes <code>MLArrays</code> into file created from
     * <code>filepath</code>.
     *
     * @param filepath
     *            the absolute file path of a MAT-file to which data is written
     * @param data
     *            the collection of <code>{@link MLArray}</code> objects
     * @throws IOException
     *             if error occurred during MAT-file writing
     */
    public synchronized void write(String filepath, Collection<MLArray> data) throws IOException {
        this.write(new File(filepath), data);
    }

    /**
     * Writes <code>MLArrays</code> into <code>File</code>
     *
     * @param file
     *            the MAT-file to which data is written
     * @param data
     *            the collection of <code>{@link MLArray}</code> objects
     * @throws IOException
     *             if error occurred during MAT-file writing
     */
    public synchronized void write(File file, Collection<MLArray> data) throws IOException {
        this.fos = new FileOutputStream(file);
        this.write(data);
    }

    /**
     * Writes <code>MLArrays</code> into <code>WritableByteChannel</code>.
     *
     * @param channel
     *            the channel to write to
     * @param data
     *            the collection of <code>{@link MLArray}</code> objects
     * @throws IOException
     *             if writing fails
     */
    private synchronized void write(Collection<MLArray> data) throws IOException {
        try {
            // Write header.
            this.writeHeader();

            // Write data.
            for (MLArray matrix : data) {
                // Prepare buffer for MATRIX data.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                // Write MATRIX bytes into buffer.
                matrix.writeMatrix(dos);

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
                ByteBuffer buf = ByteBuffer.allocate(2 * 4 /* Int size */ + compressedBytes.length);
                buf.putInt(MatDataTypes.miCOMPRESSED);
                buf.putInt(compressedBytes.length);
                buf.put(compressedBytes);

                buf.rewind();
                System.out.println(buf.remaining());
                this.fos.write(buf.array(), 0, buf.remaining());
            }
        } catch (IOException e) {
            throw e;
        } finally {
            this.fos.close();
        }
    }

    /**
     * Writes MAT-file header into <code>OutputStream</code>
     * @throws IOException
     */
    protected void writeHeader() throws IOException {
        DataOutputStream dos = new DataOutputStream(this.fos);

        MatFileHeader header = MatFileHeader.createHeader();

        // Write descriptive text.
        char[] description = header.getDescription().toCharArray();
        // The maximum length for the description is 116 characters.
        int maxLength = 116;
        int length = description.length;
        length = length > maxLength ? maxLength : length;

        for (int i = 0; i < length; ++i)
            dos.write((byte)description[i]);

        // Write description padding.
        int padding = maxLength - length;
        dos.write(new byte[padding], 0, padding);

        // Write subsys data offset (8-bytes).
        dos.writeLong(0);

        // Write version (2-bytes).
        dos.writeShort(header.getVersion());

        // Write the endian indicator (2-bytes).
        dos.write(header.getEndianIndicator(), 0, 2);
    }
}
