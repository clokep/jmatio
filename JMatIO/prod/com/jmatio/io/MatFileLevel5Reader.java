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
import java.util.zip.InflaterInputStream;

import com.jmatio.common.MatLevel5DataTypes;
import com.jmatio.types.ByteStorageSupport;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLEmptyArray;
import com.jmatio.types.MLFunctionHandle;
import com.jmatio.types.MLInt16;
import com.jmatio.types.MLInt32;
import com.jmatio.types.MLInt64;
import com.jmatio.types.MLInt8;
import com.jmatio.types.MLJavaObject;
import com.jmatio.types.MLLogical;
import com.jmatio.types.MLNumericArray;
import com.jmatio.types.MLObject;
import com.jmatio.types.MLOpaque;
import com.jmatio.types.MLSingle;
import com.jmatio.types.MLSparse;
import com.jmatio.types.MLStructure;
import com.jmatio.types.MLUInt16;
import com.jmatio.types.MLUInt32;
import com.jmatio.types.MLUInt64;
import com.jmatio.types.MLUInt8;

/**
 * Reads Level 5 MAT-files into <code>MLArray</code> objects.
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
public class MatFileLevel5Reader extends MatFileReader {
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
    public MatFileLevel5Reader(String fileName) throws FileNotFoundException, IOException {
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
    public MatFileLevel5Reader(String fileName, MatFileFilter filter) throws IOException {
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
    public MatFileLevel5Reader(File file) throws IOException {
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
    public MatFileLevel5Reader(File file, MatFileFilter filter) throws IOException {
        this();

        read(file, filter, MatFileReader.MEMORY_MAPPED_FILE);
    }

    public MatFileLevel5Reader() {
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

    private static class _ByteArrayOutputStream extends ByteArrayOutputStream {
        _ByteArrayOutputStream() {
            super();
        }

        _ByteArrayOutputStream(int initialSize) {
            super(initialSize);
        }

        byte[] getReferenceToByteArray() {
            return this.buf;
        }
    }

    private static class _InputStreamFromBuffer extends InputStream {
        private ByteBuffer buf;
        private int limit;

        public _InputStreamFromBuffer(final ByteBuffer buf, final int limit) {
            this.buf = buf;
            this.limit = limit;
        }

        @Override
        public synchronized int read() throws IOException {
            throw new RuntimeException("Not yet implemented");
        }

        public synchronized int read(byte[] bytes, int off, int len) throws IOException {
            if (!(limit > 0))
                return -1;
            len = Math.min(len, limit);
            // Read only what's left
            buf.get(bytes, off, len);
            limit -= len;
            return len;
        }
    }

    /**
     * Decompresses (inflates) bytes from input stream. Stream marker is being set at +<code>numOfBytes</code>
     * position of the stream.
     *
     * @param is
     *            input byte buffer
     * @param numOfBytes
     *            number of bytes to be read
     * @return new <code>ByteBuffer</code> with inflated block of data
     * @throws IOException
     *             when error occurs while reading or inflating the buffer .
     */
    private ByteBuffer inflate(final ByteBuffer buf, final int numOfBytes) throws IOException {
        if (buf.remaining() < numOfBytes)
            throw new MatlabIOException("Compressed buffer length miscalculated!");

        // Instead of standard Inflater class instance I use an inflater input
        // stream... gives a great boost to the performance.
        InflaterInputStream iis = new InflaterInputStream(new _InputStreamFromBuffer(buf, numOfBytes));

        // Process data decompression.
        byte[] result = new byte[128];
        _ByteArrayOutputStream baos = new _ByteArrayOutputStream(numOfBytes);
        int i;
        try {
            do {
                i = iis.read(result, 0, result.length);
                int len = Math.max(0, i);
                baos.write(result, 0, len);
            } while (i > 0);
        } catch (IOException e) {
            throw new MatlabIOException("Could not decompress data: " + e );
        } finally {
            iis.close();
        }
        // Create a ByteBuffer from the deflated data.
        ByteBuffer out = ByteBuffer.wrap(baos.getReferenceToByteArray(), 0, baos.size());
        // With proper byte ordering
        out.order(byteOrder);
        return out;
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

        switch (tag.type) {
            case MatLevel5DataTypes.miCOMPRESSED:
                // Inflate and recurse.
                this.readData(this.inflate(buf, tag.size));
                break;
            case MatLevel5DataTypes.miMATRIX:
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
                break;
            default:
                throw new MatlabIOException("Incorrect data tag: " + tag);
        }
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
        ISMatTag tag;

        // Read flags.
        int[] flags = this.readFlags(buf);
        int attributes = (flags.length != 0) ? flags[0] : 0;
        boolean global = MatLevel5DataTypes.isGlobal(attributes);
        int nzmax = (flags.length != 0) ? flags[1] : 0;
        int type = attributes & 0xff;

        // Read array dimensions.
        int[] dims = this.readDimension(buf);

        // Read array name.
        String name = this.readName(buf);

        // If this array is filtered out return immediately
        if (isRoot && !this.filter.matches(name))
            return null;

        // Read data >> consider changing it to stategy pattern
        if (type == MatLevel5DataTypes.mxSTRUCT_CLASS ||
            type == MatLevel5DataTypes.mxOBJECT_CLASS) {
            MLStructure struct;

            if (type == MatLevel5DataTypes.mxSTRUCT_CLASS)
                struct = new MLStructure(name, dims, global);
            else {
                // A MLObject has an additional field: the class name.
                struct = new MLObject(name, dims, global);

                // Read the class name.
                tag = new ISMatTag(buf);
                String className = tag.readToString();
                ((MLObject)struct).setClassName(className);
            }

            // Maximum field name length. This subelement always uses the
            // compressed data element format when created by Matlab.
            tag = new ISMatTag(buf);
            int maxlen = tag.readToIntArray()[0];

            // Read fields data as Int8.
            tag = new ISMatTag(buf);
            // Calculate number of fields.
            int numOfFields = tag.size / maxlen;

            String[] fieldNames = new String[numOfFields];
            byte[] names = new byte[maxlen];
            for (int i = 0; i < numOfFields; ++i) {
                buf.get(names);
                fieldNames[i] = this.zeroEndByteArrayToString(names);
            }
            buf.position(buf.position() + tag.padding);

            // Read fields.
            for (int index = 0; index < struct.getM() * struct.getN(); ++index) {
                for (int i = 0; i < numOfFields; ++i) {
                    // Read matrix recursively
                    tag = new ISMatTag(buf);
                    if (tag.size > 0) {
                        MLArray fieldValue = this.readMatrix(buf, false);
                        struct.setField(fieldNames[i], fieldValue, index);
                    } else
                        struct.setField(fieldNames[i], new MLEmptyArray(), index);
                }
            }
            mlArray = struct;
        } else if (type == MatLevel5DataTypes.mxCELL_CLASS) {
            MLCell cell = new MLCell(name, dims, global);
            for (int i = 0; i < cell.getSize(); ++i) {
                tag = new ISMatTag(buf);
                if (tag.size > 0) {
                    // Read matrix recursively
                    MLArray cellmatrix = this.readMatrix(buf, false);
                    cell.set(cellmatrix, i);
                } else
                    cell.set(new MLEmptyArray(), i);
            }
            mlArray = cell;
        } else if (type == MatLevel5DataTypes.mxCHAR_CLASS) {
            MLChar mlchar = new MLChar(name, dims, global);

            // Read the characters.
            tag = new ISMatTag(buf);
            char[] ac = tag.readToCharArray();
            for (int i = 0; i < ac.length; ++i)
                mlchar.setChar(ac[i], i);
            mlArray = mlchar;
        } else if (type == MatLevel5DataTypes.mxOPAQUE_CLASS) {
            this.hasOpaque = true;

            // The "name" field is actually used by MATLAB to store the class
            // type; e.g. java or MCOS (containers.Map).
            String classType = name;

            // The dimensions field is used to store the variable name.
            byte[] nn = new byte[dims.length];
            for (int i = 0; i < dims.length; ++i)
                nn[i] = (byte)dims[i];
            name = new String(nn);

            // Read the class name.
            tag = new ISMatTag(buf);
            String className = tag.readToString();

            MLOpaque opaque = new MLOpaque(name, classType, className, global);

            MLArray opaquematrix;
            // Opaque contains one other miMATRIX.
            tag = new ISMatTag(buf);
            System.out.println("Size: " + tag.size + " Type: " + tag.type);
            if (tag.size > 0) {
                opaquematrix = this.readMatrix(buf, false);
                opaque.set(((MLNumericArray<?>)opaquematrix).getRealByteBuffer());
            } else
                opaque.set(ByteBuffer.allocate(0));

            if (opaque.isJavaObject())
                opaque = new MLJavaObject(opaque);

            System.out.println(buf.remaining());
            //if (opaque.getClassType().equals(MLOpaque.CONTAINERS_MAP_TYPE)) {
                //tag = new ISMatTag(buf);
                //System.out.println("Size: " + tag.size + " Type: " + tag.type);
                //opaquematrix = this.readMatrix(buf, false);
                //System.out.println("Type: " + MLArray.typeToString(opaquematrix.getType()));
                //System.out.println(opaquematrix);
            //}

            mlArray = opaque;
        //} else if (type == MatLevel5DataTypes.mxFUNCTION_CLASS) {
        //    MLFunctionHandle function = new MLFunctionHandle(name, dims, global);
        //
        //    // Function handles contains one other miMATRIX.
        //    tag = new ISMatTag(buf);
        //    if (tag.size > 0) {
        //        // The matrix is a structure with some unintersting stuff in it:
        //        //  MLChar      matlabroot      [ = 'C:\MATLAB2012a' ]
        //        //  MLChar      seperator       [ = '\' ]
        //        //  MLChar      sentinel        [ = '@' ]
        //        //  MLStructure function_handle
        //        //      	MLChar      function
        //        //          MLChar      type
        //        //          MLChar      file
        //        //          MLOpaque    workspace
        //        MLStructure functionmatrix = (MLStructure)this.readMatrix(buf, false);
        //        System.out.println(functionmatrix);
        //        System.out.println(functionmatrix.contentToString());
        //
        //        MLArray temp = functionmatrix.getField("matlabroot");
        //        System.out.println(temp);
        //        System.out.println(temp.contentToString());
        //
        //        temp = functionmatrix.getField("separator");
        //        System.out.println(temp);
        //        System.out.println(temp.contentToString());
        //
        //        temp = functionmatrix.getField("sentinel");
        //        System.out.println(temp);
        //        System.out.println(temp.contentToString());
        //
        //        functionmatrix = (MLStructure)functionmatrix.getField("function_handle");
        //        System.out.println(functionmatrix);
        //        System.out.println(functionmatrix.contentToString());
        //
        //        temp = functionmatrix.getField("function");
        //        System.out.println(temp);
        //        System.out.println(temp.contentToString());
        //
        //        temp = functionmatrix.getField("type");
        //        System.out.println(temp);
        //        System.out.println(temp.contentToString());
        //
        //        temp = functionmatrix.getField("file");
        //        System.out.println(temp);
        //        System.out.println(temp.contentToString());
        //
        //        temp = functionmatrix.getField("workspace");
        //        System.out.println(temp);
        //        System.out.println(temp.contentToString());
        //
        //        temp = ((MLOpaque)temp).get();
        //        System.out.println(temp);
        //        System.out.println(temp.contentToString());
        //
        //        //opaque.set(functionmatrix);
        //    }/* else
        //        opaque.set(new MLEmptyArray());*/
        //
        //    mlArray = null;
        } else {
            boolean complex = MatLevel5DataTypes.isComplex(attributes);
            boolean logical = MatLevel5DataTypes.isLogical(attributes);
            
            if (type == MatLevel5DataTypes.mxSPARSE_CLASS) {
                MLSparse sparse = new MLSparse(name, dims, nzmax, complex, global);
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
                    while (count < jc[column + 1]) {
                        sparse.setReal(ad1[count], ir[count], column);
                        ++count;
                    }
                }
                System.out.println(count);
    
                // Read pi (imaginary part)
                if (sparse.isComplex()) {
                    tag = new ISMatTag(buf);
                    double[] ad2 = tag.readToDoubleArray();
    
                    count = 0;
                    for (int column = 0; column < sparse.getN(); column++) {
                        while (count < jc[column+1]) {
                            sparse.setImaginary(ad2[count], ir[count], column);
                            ++count;
                        }
                    }
                }
                System.out.println(count);
                mlArray = sparse;
            } else {
                // At this point we should have a numeric class.
                switch (type) {
                    case MatLevel5DataTypes.mxDOUBLE_CLASS:
                        mlArray = new MLDouble(name, dims, complex, global, logical);
                        break;
                    case MatLevel5DataTypes.mxSINGLE_CLASS:
                        mlArray = new MLSingle(name, dims, complex, global, logical);
                        break;
                    case MatLevel5DataTypes.mxUINT8_CLASS:
                        mlArray = new MLUInt8(name, dims, complex, global, logical);
                        break;
                    case MatLevel5DataTypes.mxINT8_CLASS:
                        mlArray = new MLInt8(name, dims, complex, global, logical);
                        break;
                    case MatLevel5DataTypes.mxUINT16_CLASS:
                        mlArray = new MLUInt16(name, dims, complex, global, logical);
                        break;
                    case MatLevel5DataTypes.mxINT16_CLASS:
                        mlArray = new MLInt16(name, dims, complex, global, logical);
                        break;
                    case MatLevel5DataTypes.mxUINT32_CLASS:
                        mlArray = new MLUInt32(name, dims, complex, global, logical);
                        break;
                    case MatLevel5DataTypes.mxINT32_CLASS:
                        mlArray = new MLInt32(name, dims, complex, global, logical);
                        break;
                    case MatLevel5DataTypes.mxINT64_CLASS:
                        mlArray = new MLInt64(name, dims, complex, global, logical);
                        break;
                    case MatLevel5DataTypes.mxUINT64_CLASS:
                        mlArray = new MLUInt64(name, dims, complex, global, logical);
                        break;
                    default:
                        throw new MatlabIOException("Unsupported matlab array class: " + MatLevel5DataTypes.matrixTypeToString(type) + " (" + type + ").");
                }
    
                // Read real.
                tag = new ISMatTag(buf);
                tag.readToByteBuffer(((MLNumericArray<?>) mlArray).getRealByteBuffer(),
                                     (MLNumericArray<?>) mlArray);
                // Read complex.
                if (mlArray.isComplex()) {
                    tag = new ISMatTag(buf);
                    tag.readToByteBuffer(((MLNumericArray<?>) mlArray).getImaginaryByteBuffer(),
                                         (MLNumericArray<?>) mlArray);
                }
                if (mlArray.isLogical()) {
                    // If it's not complex and is logical, convert it to a logical
                    // array.
                    mlArray = new MLLogical((MLNumericArray<?>)mlArray);
                }
            }
        }

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
    private int[] readFlags(ByteBuffer buf) throws IOException {
        ISMatTag tag = new ISMatTag(buf);
        int[] x = tag.readToIntArray();
        return x;
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
        ISMatTag tag = new ISMatTag(buf);
        return tag.readToIntArray();
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
        ISMatTag tag = new ISMatTag(buf);
        return tag.readToString();
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
        // If any of the first four bytes are a 0, consider it a Level 4
        // matfile.
        if (buf.get(0) == 0 || buf.get(1) == 0 || buf.get(2) == 0 ||
            buf.get(3) == 0) {
            throw new MatlabIOException("Cannot read Level 4 MAT-files.");
        }

        // Descriptive text 116 bytes.
        byte[] descriptionBuffer = new byte[116];
        buf.get(descriptionBuffer);

        String description = this.zeroEndByteArrayToString(descriptionBuffer);

        if (!description.matches("MATLAB 5.0 MAT-file.*"))
            throw new MatlabIOException("This is not a valid MATLAB 5.0 MAT-file.");

        // Subsyst data offset 8 bytes.
        buf.position(buf.position() + 8);

        // Version 2 bytes.
        int version;
        byte[] bversion = new byte[2];
        buf.get(bversion);

        // Endian indicator 2 bytes.
        byte[] endianIndicator = new byte[2];
        buf.get(endianIndicator);

        // Program reading the MAT-file must perform byte swapping to interpret
        // the data in the MAT-file correctly.
        if ((char)endianIndicator[0] == 'I' && (char)endianIndicator[1] == 'M') {
            this.byteOrder = ByteOrder.LITTLE_ENDIAN;
            version = bversion[1] & 0xff | bversion[0] << 8;
        } else {
            this.byteOrder = ByteOrder.BIG_ENDIAN;
            version = bversion[0] & 0xff | bversion[1] << 8;
        }

        buf.order(this.byteOrder);

        this.matFileHeader = new MatFileHeader(description, version, endianIndicator);
    }
}
