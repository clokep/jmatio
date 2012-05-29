package com.jmatio.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
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
        this((new FileOutputStream(file)).getChannel(), data);
    }

    /**
     * Writes MLArrays into <code>OuputSteram</code>.
     *
     * Writes MAT-file header and compressed data (<code>miCOMPRESSED</code>).
     *
     * @param channel <code>OutputStream</code>
     * @param data <code>Collection</code> of <code>MLArray</code> elements
     * @throws IOException
     */
    public MatFileWriter(WritableByteChannel channel, Collection<MLArray> data) throws IOException {
        this.write(channel, data);
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
        FileOutputStream fos = new FileOutputStream(file);

        try {
            this.write(fos.getChannel(), data);
        } catch (IOException e) {
            throw e;
        } finally {
            fos.close();
        }
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
    private synchronized void write(WritableByteChannel channel, Collection<MLArray> data) throws IOException {
        try {
            // Write header.
            this.writeHeader(channel);

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
                ByteBuffer buf = ByteBuffer.allocateDirect(2 * 4 /* Int size */ + compressedBytes.length);
                buf.putInt(MatDataTypes.miCOMPRESSED);
                buf.putInt(compressedBytes.length);
                buf.put(compressedBytes);

                buf.flip();
                channel.write(buf);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            channel.close();
        }
    }

    /**
     * Writes MAT-file header into <code>OutputStream</code>
     * @param os <code>OutputStream</code>
     * @throws IOException
     */
    private void writeHeader(WritableByteChannel channel) throws IOException {
        // Write descriptive text.
        MatFileHeader header = MatFileHeader.createHeader();
        char[] dest = new char[116];
        char[] src = header.getDescription().toCharArray();
        System.arraycopy(src, 0, dest, 0, src.length);

        byte[] endianIndicator = header.getEndianIndicator();

        ByteBuffer buf = ByteBuffer.allocateDirect(dest.length * 2 /* Char size */ + 2 + endianIndicator.length);

        for (int i = 0; i < dest.length; ++i)
            buf.put((byte)dest[i]);
        // Write subsyst data offset.
        buf.position(buf.position() + 8);

        // Write version.
        int version = header.getVersion();
        buf.put((byte)(version >> 8));
        buf.put((byte)version);

        buf.put(endianIndicator);

        buf.flip();
        channel.write(buf);
    }
}
