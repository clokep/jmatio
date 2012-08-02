package com.jmatio.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.jmatio.common.MatLevel4DataTypes;
import com.jmatio.types.ByteStorageSupport;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLEmptyArray;
import com.jmatio.types.MLSparse;

/**
 * Reads Level 4 MAT-files into <code>MLArray</code> objects.
 *
 * Usage:
 * <pre><code>
 * // Read in the file.
 * MatFileReader mfr = new MatFileReader("mat_file.mat");
 *
 * // Get array of a name "my_array" from file.
 * MLArray mlArrayRetrived = mfr.getMLArray("my_array");
 *
 * // Or get the collection of all arrays that were stored in the file.
 * Map content = mfr.getContent();
 * </pre></code>
 *
 * @see com.jmatio.io.MatFileFilter
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MatFileLevel4Reader extends MatFileReader {
    /**
     * The MAT-file header.
     */
    private MatFileHeader matFileHeader;

    /**
     * If an mxOPAQUE_CLASS type matrix is found, then there is an extra data
     * matrix at the end that must be read.
     */
    private boolean hasOpaque = false;

    /**
     * Creates an instance of <code>MatFileReader</code> and reads MAT-file
     * from the location given as <code>fileName</code>.
     *
     * This method reads a MAT-file without filtering.
     *
     * @param fileName the MAT-file path <code>String</code>
     * @throws IOException when error occurred while processing the file.
     */
    public MatFileLevel4Reader(String fileName) throws FileNotFoundException, IOException {
        this(new File(fileName), new MatFileFilter());
    }
    /**
     * Creates instance of <code>MatFileReader</code> and reads MAT-file
     * from location given as <code>fileName</code>.
     *
     * Results are filtered by <code>MatFileFilter</code>. Arrays that do not meet
     * filter match condition will not be available in results.
     *
     * @param fileName the MAT-file path <code>String</code>
     * @param filter array name filter.
     * @throws IOException when error occurred while processing the file.
     */
    public MatFileLevel4Reader(String fileName, MatFileFilter filter) throws IOException {
        this(new File(fileName), filter);
    }

    /**
     * Creates instance of <code>MatFileReader</code> and reads MAT-file
     * from <code>file</code>.
     *
     * This method reads MAT-file without filtering.
     *
     * @param file the MAT-file
     * @throws IOException when error occurred while processing the file.
     */
    public MatFileLevel4Reader(File file) throws IOException {
        this(file, new MatFileFilter());
    }

    /**
     * Creates instance of <code>MatFileReader</code> and reads MAT-file from
     * <code>file</code>.
     * <p>
     * Results are filtered by <code>MatFileFilter</code>. Arrays that do not
     * meet filter match condition will not be available in results.
     * <p>
     * <i>Note: this method reads file using the memory mapped file policy, see
     * notes to </code>{@link #read(File, MatFileFilter, int)}</code>
     *
     * @param file
     *            the MAT-file
     * @param filter
     *            array name filter.
     * @throws IOException
     *             when error occurred while processing the file.
     */
    public MatFileLevel4Reader(File file, MatFileFilter filter) throws IOException {
        this();

        read(file, filter, MatFileReader.MEMORY_MAPPED_FILE);
    }

    public MatFileLevel4Reader() {
        this.filter = new MatFileFilter();
        this.data = new LinkedHashMap<String, MLArray>();
    }

    /**
     * Reads the content of a MAT-file and returns the mapped content.
     * <p>
     * This method calls
     * <code>read(file, new MatFileFilter(), MallocPolicy.MEMORY_MAPPED_FILE)</code>.
     *
     * @param file
     *            a valid MAT-file file to be read
     * @return the same as <code>{@link #getContent()}</code>
     * @throws IOException
     *             if error occurs during file processing
     */
    public synchronized Map<String, MLArray> read(File file) throws IOException {
       return read(file, new MatFileFilter(), MatFileReader.MEMORY_MAPPED_FILE);
    }
    /**
     * Reads the content of a MAT-file and returns the mapped content.
     * <p>
     * This method calls
     * <code>read(file, new MatFileFilter(), policy)</code>.
     *
     * @param file
     *            a valid MAT-file file to be read
     * @param policy
     *            the file memory allocation policy
     * @return the same as <code>{@link #getContent()}</code>
     * @throws IOException
     *             if error occurs during file processing
     */
    public synchronized Map<String, MLArray> read(File file, int policy) throws IOException {
        return read(file, new MatFileFilter(), policy);
    }

    /**
     * Gets MAT-file header
     *
     * @return - a <code>MatFileHeader</code> object
     */
    public MatFileHeader getMatFileHeader() {
        return this.matFileHeader;
    }

    /**
     * Reads data form byte buffer. Searches for either
     * <code>miCOMPRESSED</code> data or <code>miMATRIX</code> data.
     *
     * Compressed data are inflated and the product is recursively passed back
     * to this same method.
     *
     * Modifies <code>buf</code> position.
     *
     * @param buf
     *            input byte buffer
     * @throws IOException when error occurs while reading the buffer.
     */
    protected void readData(ByteBuffer buf) throws IOException {
        // Read data.
        ISMatTag tag = new ISMatTag(buf);
        // Read in the matrix.
        int pos = buf.position();

        MLArray element = this.readMatrix(buf, true);

        if (element != null)
            data.put(element.getName(), element);
        else {
            int read = buf.position() - pos;
            int toRead = tag.size - read;
            buf.position(buf.position() + toRead);
        }
        int read = buf.position() - pos;
        int toRead = tag.size - read;

        if (toRead != 0)
            throw new MatlabIOException("Matrix was not read fully! " + toRead + " remaining in the buffer.");
    }

    /**
     * Reads miMATRIX from from input stream.
     *
     * If reading was not finished (which is normal for filtered results)
     * returns <code>null</code>.
     *
     * Modifies <code>buf</code> position to the position when reading
     * finished.
     *
     * Uses recursive processing for some ML**** data types.
     *
     * @param buf
     *            input byte buffer
     * @param isRoot
     *            when <code>true</code> informs that if this is a top level
     *            matrix
     * @return <code>MLArray</code> or <code>null</code> if matrix does
     *         not match <code>filter</code>
     * @throws IOException when error occurs while reading the buffer.
     */
    protected MLArray readMatrix(ByteBuffer buf, boolean isRoot) throws IOException {
        // The result.
        MLArray mlArray;

        // Read flags.
        int attributes = 0;
        // XXX set whether it's complex here!
        int type = (int)buf.getLong();

        // Get just the thousand's digit (endianess).
        int m = type / 1000 % 10;
        if (m == 0)
            this.byteOrder = ByteOrder.LITTLE_ENDIAN;
        else if (m == 1)
            this.byteOrder = ByteOrder.BIG_ENDIAN;
        else
            throw new MatlabIOException("Unsupported byte order: " + m);

        // Ensure the hundred's digit is 0.
        if (type / 100 % 10 != 0)
            throw new MatlabIOException("0 isn't 0!");

        // Get just the ten's digit (data format).
        int format = type / 10 % 10;

        // Get the one's digit (matrix type).
        type = type % 10;

        // Read array dimensions.
        int[] dims = this.readDimension(buf);
        
        // If this flag is 1, there is imaginary data.
        long imgf = buf.getLong();

        // Read array name.
        String name = this.readName(buf);

        // If this array is filtered out, return immediately.
        // XXX we'll need to move buf past this array.
        if (!this.filter.matches(name))
            return null;

        if (type == MatLevel4DataTypes.mxNUMERIC_CLASS) {
            // Numeric matrix.
            mlArray = new MLDouble(name, dims);
        } else if (type == MatLevel4DataTypes.mxTEXT_CLASS) {
            // Text matrix.
            mlArray = new MLChar(name, dims);
        } else if (type == MatLevel4DataTypes.mxSPARSE_CLASS) {
            // Sparse matrix.
            //mlArray = new MLSparse(name, dims, nzmax);
            mlArray = new MLEmptyArray();
        } else
            throw new MatlabIOException("Unsupported matlab array class: " + MatLevel4DataTypes.typeToString(type));

        // Convert the Level 4 format to a Level 5 type.
        /*int[] formatToType = new int[]{MatLevel4DataTypes.miDOUBLE,
                                       MatLevel4DataTypes.miSINGLE,
                                       MatLevel4DataTypes.miINT32,
                                       MatLevel4DataTypes.miINT16,
                                       MatLevel4DataTypes.miUINT16,
                                       MatLevel4DataTypes.miUINT8};
        if (format < 0 || format > formatToType.length)
            throw new MatlabIOException("Unsupported data storage format: " + format);
        format = formatToType[format];

        MatFileInputStream mfis = new MatFileInputStream(buf, format);
        mfis.readToByteBuffer(((MLNumericArray<?>) mlArray).getImaginaryByteBuffer(), dims[0] * dims[1], (MLNumericArray<?>) mlArray);
        */

        // Read data >> consider changing it to stategy pattern
        /*if (type == MatLevel4DataTypes.mxCHAR_CLASS) {
            MLChar mlchar = new MLChar(name, dims, type, attributes);

            // Read the characters.
            tag = new ISMatTag(buf);
            char[] ac = tag.readToCharArray();
            for ( int i = 0; i < ac.length; i++ )
                mlchar.setChar(ac[i], i);
            mlArray = mlchar;
        } else if (type == MatLevel4DataTypes.mxSPARSE_CLASS) {
            MLSparse sparse = new MLSparse(name, dims, attributes, nzmax);
            // Read ir (row indices).
            tag = new ISMatTag(buf);
            int[] ir = tag.readToIntArray();
            // Read jc (column count).
            tag = new ISMatTag(buf);
            int[] jc = tag.readToIntArray();

            // Read pr (real part).
            tag = new ISMatTag(buf);
            double[] ad1 = tag.readToDoubleArray();
            int count = 0;
            for (int column = 0; column < sparse.getN(); column++) {
                while(count < jc[column + 1]) {
                    sparse.setReal(ad1[count], ir[count], column);
                    ++count;
                }
            }

            // Read pi (imaginary part)
            if (sparse.isComplex()) {
                tag = new ISMatTag(buf);
                double[] ad2 = tag.readToDoubleArray();

                count = 0;
                for (int column = 0; column < sparse.getN(); column++) {
                    while(count < jc[column+1]) {
                        sparse.setImaginary(ad2[count], ir[count], column);
                        ++count;
                    }
                }
            }
            mlArray = sparse;
        } else {
            // At this point we should have a numeric class.
            mlArray = new MLDouble(name, dims, type, attributes);


            // Read complex.
            if (mlArray.isComplex()) {
                tag = new ISMatTag(buf);
                tag.readToByteBuffer( ((MLNumericArray<?>) mlArray).getImaginaryByteBuffer(),
                        (MLNumericArray<?>) mlArray );
            }
        } else {
            throw new MatlabIOException("Unsupported matlab array class: " + MLArray.typeToString(type));
        }*/

        return mlArray;
    }

    /**
     * Reads Matrix flags.
     *
     * Modifies <code>buf</code> position.
     *
     * @param buf <code>ByteBuffer</code>
     * @return flags int array
     * @throws IOException if reading from buffer fails
     */
    protected int[] readFlags(ByteBuffer buf) throws IOException {
        // Level 4 MAT-files do not have flags.
        return new int[]{0, 0};
    }

    /**
     * Reads Matrix dimensions.
     *
     * Modifies <code>buf</code> position.
     *
     * @param buf <code>ByteBuffer</code>
     * @return dimensions int array
     * @throws IOException if reading from buffer fails
     */
    protected int[] readDimension(ByteBuffer buf) throws IOException {
        long mrows = buf.getLong();
        long nrows = buf.getLong();
        return new int[]{(int)mrows, (int)nrows};
    }

    /**
     * Reads Matrix name.
     *
     * Modifies <code>buf</code> position.
     *
     * @param buf <code>ByteBuffer</code>
     * @return name <code>String</code>
     * @throws IOException if reading from buffer fails
     */
    protected String readName(ByteBuffer buf) throws IOException {
        // The number of ASCII characters in the name + 1.
        long namlen = buf.getLong();
        
        byte[] name = new byte[(int)namlen];
        buf.get(name);
        
        return this.zeroEndByteArrayToString(name);
    }
    /**
     * Reads MAT-file header.
     *
     * Modifies <code>buf</code> position.
     *
     * @param buf
     *            <code>ByteBuffer</code>
     * @throws IOException
     *             if reading from buffer fails or if this is not a valid
     *             MAT-file
     */
    protected void readHeader(ByteBuffer buf) throws IOException {
        // Level 4 MAT-files do not have a header.
    }
}
