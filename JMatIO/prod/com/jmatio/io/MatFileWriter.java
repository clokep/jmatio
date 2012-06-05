package com.jmatio.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import com.jmatio.common.MatDataTypes;
import com.jmatio.common.VariableUtils;
import com.jmatio.types.MLArray;

/**
 * MAT-file writer.
 *
 * Usage:
 * <pre><code>
 * // 1. Create example arrays.
 * double[] src = new double[] {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
 * MLDouble mlDouble = new MLDouble("double_arr", src, 3);
 * MLChar mlChar = new MLChar("char_arr", "I am dummy");
 *
 * // 2. Write arrays to file.
 * new MatFileWriter("mat_file.mat", Arrays.asList((MLArray)list));
 * </code></pre>
 *
 * This is "equal" to Matlab commands:
 * <pre><code>
 * >> double_arr = [1 2; 3 4; 5 6];
 * >> char_arr = 'I am dummy';
 * >>
 * >> save('mat_file.mat', 'double_arr', 'char_arr');
 * </pre></code>
 *
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MatFileWriter {
    protected FileOutputStream fos;

    /**
     * Creates the new <code>{@link MatFileWriter}</code> instance
     */
    public MatFileWriter() { }

    /**
     * Writes MLArrays into file given by <code>fileName</code>.
     *
     * @param fileName name of ouput file
     * @param data <code>Collection</code> of <code>MLArray</code> elements
     * @throws IOException
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
            for (MLArray matrix : data)
                this.writeMatrix(matrix);
        } catch (IOException e) {
            throw e;
        } finally {
            this.fos.close();
        }
    }

    protected synchronized void writeMatrix(MLArray data) throws IOException {
        // Prepare buffer for MATRIX data.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream( baos );
        // Write MATRIX bytes into buffer.
        data.writeMatrix(dos);

        byte[] compressed = this.deflate(baos.toByteArray());

        // Write COMPRESSED tag and compressed data into output channel.
        OSMatTag tag = new OSMatTag(MatDataTypes.miCOMPRESSED, compressed);
        tag.writeTo(this.fos);
    }

    /**
     * Compresses (deflates) bytes for the output stream.
     *
     * @param is
     *            input byte array
     * @return new <code>byte[]</code> with deflated block of data
     * @throws IOException
     *             when error occurs while reading or inflating the buffer .
     */
    protected byte[] deflate(final byte[] input) throws IOException {
        // Compress data to save storage.
        Deflater compresser = new Deflater();

        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new DeflaterOutputStream(compressed, compresser));

        dout.write(input);

        dout.close();
        compressed.close();

        return compressed.toByteArray();
    }

    /**
     * Writes MAT-file header into <code>OutputStream</code>
     * @throws IOException
     */
    protected void writeHeader() throws IOException {
        DataOutputStream dos = new DataOutputStream(this.fos);

        MatFileHeader header = new MatFileHeader();

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
