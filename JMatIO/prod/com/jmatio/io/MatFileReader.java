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

import com.jmatio.common.MatDataTypes;
import com.jmatio.types.ByteStorageSupport;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLEmptyArray;
import com.jmatio.types.MLInt16;
import com.jmatio.types.MLInt32;
import com.jmatio.types.MLInt64;
import com.jmatio.types.MLInt8;
import com.jmatio.types.MLLogical;
import com.jmatio.types.MLNumericArray;
import com.jmatio.types.MLSingle;
import com.jmatio.types.MLSparse;
import com.jmatio.types.MLStructure;
import com.jmatio.types.MLUInt16;
import com.jmatio.types.MLUInt32;
import com.jmatio.types.MLUInt64;
import com.jmatio.types.MLUInt8;

/**
 * MAT-file reader. Reads MAT-file into <code>MLArray</code> objects.
 *
 * Usage:
 * <pre><code>
 * //read in the file
 * MatFileReader mfr = new MatFileReader( "mat_file.mat" );
 *
 * //get array of a name "my_array" from file
 * MLArray mlArrayRetrived = mfr.getMLArray( "my_array" );
 *
 * //or get the collection of all arrays that were stored in the file
 * Map content = mfr.getContent();
 * </pre></code>
 *
 * @see com.jmatio.io.MatFileFilter
 * @author Wojciech Gradkowski (<a href="mailto:wgradkowski@gmail.com">wgradkowski@gmail.com</a>)
 */
public class MatFileReader {
    public static final int MEMORY_MAPPED_FILE = 1;
    public static final int DIRECT_BYTE_BUFFER = 2;
    public static final int HEAP_BYTE_BUFFER   = 4;

    /**
     * MAT-file header
     */
    private MatFileHeader matFileHeader;
    /**
     * Container for read <code>MLArray</code>s
     */
    private Map<String, MLArray> data;
    /**
     * Tells how bytes are organized in the buffer.
     */
    private ByteOrder byteOrder;
    /**
     * Array name filter
     */
    private MatFileFilter filter;
    /**
     * Creates instance of <code>MatFileReader</code> and reads MAT-file
     * from location given as <code>fileName</code>.
     *
     * This method reads MAT-file without filtering.
     *
     * @param fileName the MAT-file path <code>String</code>
     * @throws IOException when error occurred while processing the file.
     */
    public MatFileReader(String fileName) throws FileNotFoundException, IOException {
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
    public MatFileReader(String fileName, MatFileFilter filter) throws IOException {
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
    public MatFileReader(File file) throws IOException {
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
    public MatFileReader(File file, MatFileFilter filter) throws IOException {
        this();

        read(file, filter, MatFileReader.MEMORY_MAPPED_FILE);
    }

    public MatFileReader() {
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
     * Reads the content of a MAT-file and returns the mapped content.
     * <p>
     * Because of java bug <a
     * href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4724038">#4724038</a>
     * which disables releasing the memory mapped resource, additional different
     * allocation modes are available.
     * <ul>
     * <li><code>{@link #MEMORY_MAPPED_FILE}</code> - a memory mapped file</li>
     * <li><code>{@link #DIRECT_BYTE_BUFFER}</code> - a uses
     * <code>{@link ByteBuffer#allocateDirect(int)}</code> method to read in
     * the file contents</li>
     * <li><code>{@link #HEAP_BYTE_BUFFER}</code> - a uses
     * <code>{@link ByteBuffer#allocate(int)}</code> method to read in the
     * file contents</li>
     * </ul>
     * <i>Note: memory mapped file will try to invoke a nasty code to relase
     * it's resources</i>
     *
     * @param file
     *            a valid MAT-file file to be read
     * @param filter
     *            the array filter applied during reading
     * @param policy
     *            the file memory allocation policy
     * @return the same as <code>{@link #getContent()}</code>
     * @see MatFileFilter
     * @throws IOException
     *             if error occurs during file processing
     */
    private static final int DIRECT_BUFFER_LIMIT = 1 << 25;
    public synchronized Map<String, MLArray> read(File file, MatFileFilter filter,
                                                  int policy) throws IOException {
        this.filter = filter;

        // Clear the results.
        for (String key : this.data.keySet())
            this.data.remove(key);

        FileChannel roChannel = null;
        RandomAccessFile raFile = null;
        ByteBuffer buf = null;
        WeakReference<MappedByteBuffer> bufferWeakRef = null;
        try {
            // Create a read-only memory-mapped file.
            raFile = new RandomAccessFile(file, "r");
            roChannel = raFile.getChannel();
            // until java bug #4715154 is fixed I am not using memory mapped files
            // The bug disables re-opening the memory mapped files for writing
            // or deleting until the VM stops working. In real life I need to open
            // and update files
            switch ( policy ) {
                case MatFileReader.DIRECT_BYTE_BUFFER:
                    buf = ByteBuffer.allocateDirect((int)roChannel.size());
                    roChannel.read(buf, 0);
                    buf.rewind();
                    break;
                case MatFileReader.HEAP_BYTE_BUFFER:
                    final int filesize = (int)roChannel.size();
                    System.gc();
                    buf = ByteBuffer.allocate(filesize);

                    // The following two methods couldn't be used (at least under MS Windows)
                    // since they are implemented in a suboptimal way. Each of them
                    // allocates its own _direct_ buffer of exactly the same size,
                    // the buffer passed as parameter has, reads data into it and
                    // only afterwards moves data into the buffer passed as parameter.
                    // roChannel.read(buf, 0);        // ends up in outOfMemory
                    // raFile.readFully(buf.array()); // ends up in outOfMemory
                    final int numberOfBlocks = filesize / DIRECT_BUFFER_LIMIT + ((filesize % DIRECT_BUFFER_LIMIT) > 0 ? 1 : 0);
                    if (numberOfBlocks > 1) {
                        ByteBuffer tempByteBuffer = ByteBuffer.allocateDirect(DIRECT_BUFFER_LIMIT);
                        for (int block = 0; block < numberOfBlocks; ++block) {
                            tempByteBuffer.clear();
                            roChannel.read(tempByteBuffer, block * DIRECT_BUFFER_LIMIT);
                            tempByteBuffer.flip();
                            buf.put(tempByteBuffer);
                        }
                        tempByteBuffer = null;
                    } else
                        roChannel.read(buf, 0);

                    buf.rewind();
                    break;
                case MatFileReader.MEMORY_MAPPED_FILE:
                    buf = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int)roChannel.size());
                    bufferWeakRef = new WeakReference<MappedByteBuffer>((MappedByteBuffer)buf);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown file allocation policy");
            }
            // Read in file header.
            this.readHeader(buf);

            while (buf.remaining() > 0)
                readData(buf);

            return getContent();
        } catch (IOException e) {
            throw e;
        } finally {
            if (roChannel != null) {
                try {
                    roChannel.close();
                } catch (Throwable ioe){ }
            }
            if (raFile != null) {
                try {
                    raFile.close();
                } catch (Throwable ioe){ }
            }
            if (buf != null && bufferWeakRef != null && policy == MatFileReader.MEMORY_MAPPED_FILE) {
                try {
                    clean(buf);
                } catch (Exception e) {
                    int GC_TIMEOUT_MS = 1000;
                    buf = null;
                    long start = System.currentTimeMillis();
                    while (bufferWeakRef.get() != null) {
                        if (System.currentTimeMillis() - start > GC_TIMEOUT_MS) {
                            break; // A hell cannot be unmapped - hopefully GC will
                                   // Do it's job later
                        }
                        System.gc();
                        Thread.yield();
                    }
                }
            }
        }
    }

    /**
     * Workaround taken from bug <a
     * href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4724038">#4724038</a>
     * to release the memory mapped byte buffer.
     * <p>
     * Little quote from SUN: <i>This is highly inadvisable, to put it mildly.
     * It is exceedingly dangerous to forcibly unmap a mapped byte buffer that's
     * visible to Java code. Doing so risks both the security and stability of
     * the system</i>
     * <p>
     * Since the memory byte buffer used to map the file is not exposed to the
     * outside world, maybe it's save to use it without being cursed by the SUN.
     * Since there is no other solution this will do (don't trust voodoo GC
     * invocation)
     *
     * @param buffer
     *            the buffer to be unmapped
     * @throws Exception
     *             all kind of evil stuff
     */
    private void clean(final Object buffer) throws Exception {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    Method getCleanerMethod = buffer.getClass().getMethod(
                            "cleaner", new Class[0]);
                    getCleanerMethod.setAccessible(true);
                    sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod
                            .invoke(buffer, new Object[0]);
                    cleaner.clean();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
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
     * Returns list of <code>MLArray</code> objects that were inside MAT-file
     *
     * @return a <code>ArrayList</code>
     * @deprecated use <code>getContent</code> which returns a Map to provide
     *             easier access to <code>MLArray</code>s contained in MAT-file
     */
    public ArrayList<MLArray> getData() {
        return new ArrayList<MLArray>(this.data.values());
    }

    /**
     * Returns the value to which the read file maps the specified array name.
     *
     * Returns <code>null</code> if the file contains no content for this name.
     *
     * @param name name
     * @return the <code>MLArray</code> to which this file maps the specified name,
     *         or null if the file contains no content for this name.
     */
    public MLArray getMLArray(String name) {
        return this.data.get(name);
    }

    /**
     * Returns a map of <code>MLArray</code> objects that were inside MAT-file.
     *
     * MLArrays are mapped with MLArrays' names
     *
     * @return a <code>Map</code> of MLArrays mapped with their names.
     */
    public Map<String, MLArray> getContent() {
        return this.data;
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
        // stream... gives a great boost to the performance..
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
    private void readData(ByteBuffer buf) throws IOException {
        // Read data
        ISMatTag tag = new ISMatTag(buf);
        switch (tag.type) {
            case MatDataTypes.miCOMPRESSED:
                // Inflate and recurse.
                this.readData(this.inflate(buf, tag.size));
                break;
            case MatDataTypes.miMATRIX:
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
    private MLArray readMatrix(ByteBuffer buf, boolean isRoot) throws IOException {
        // The result.
        MLArray mlArray;
        ISMatTag tag;

        // Read flags.
        int[] flags = this.readFlags(buf);
        int attributes = (flags.length != 0) ? flags[0] : 0;
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
        if (type == MLArray.mxSTRUCT_CLASS) {
            MLStructure struct = new MLStructure(name, dims, type, attributes);

            // Field name length - this subelement always uses the compressed data element format.
            tag = new ISMatTag(buf);
            // Maximum field length
            int maxlen = buf.getInt();

            // Read fields data as Int8.
            tag = new ISMatTag(buf);
            // Calculate number of fields
            int numOfFields = tag.size / maxlen;

            // Padding after field names
            int padding = (tag.size % 8) != 0 ? 8 - (tag.size % 8) : 0;

            String[] fieldNames = new String[numOfFields];
            for (int i = 0; i < numOfFields; ++i) {
                byte[] names = new byte[maxlen];
                buf.get(names);
                fieldNames[i] = zeroEndByteArrayToString(names);
            }
            buf.position(buf.position() + padding);
            // Read fields
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
        } else if (type == MLArray.mxCELL_CLASS) {
            MLCell cell = new MLCell(name, dims, type, attributes);
            for (int i = 0; i < cell.getM() * cell.getN(); ++i) {
                tag = new ISMatTag(buf);
                if (tag.size > 0) {
                    // Read matrix recursively
                    MLArray cellmatrix = this.readMatrix(buf, false);
                    cell.set(cellmatrix, i);
                } else
                    cell.set(new MLEmptyArray(), i);
            }
            mlArray = cell;
        } else if (type == MLArray.mxCHAR_CLASS) {
            MLChar mlchar = new MLChar(name, dims, type, attributes);

            //read real
            tag = new ISMatTag(buf);
            char[] ac = tag.readToCharArray();
            for ( int i = 0; i < ac.length; i++ )
                mlchar.setChar( ac[i], i );
            mlArray = mlchar;
        } else if (type == MLArray.mxSPARSE_CLASS) {
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
                        count++;
                    }
                }
            }
            mlArray = sparse;
        //} else if (type == MLArray.mxOPAQUE_CLASS) {
        //    // Read ir (row indices).
        //    tag = new ISMatTag(buf);
        //    bytes = new byte[tag.size];
        //    //buf.get(bytes);
        //    System.out.println( "Class name: " + new String(tag.readToCharArray()));
        //    System.out.println( "Array name: " + name );
        //    System.out.println( "Array type: " + type);
        //
        //    byte[] nn = new byte[dims.length];
        //    for ( int i = 0; i < dims.length; i++ )
        //        nn[i] = (byte)dims[i];
        //    System.out.println( "Array name: " + new String ( nn ) );
        //
        //    readData(buf);
        //
        //    mlArray = null;
        //    break;
        } else {
            // At this point we should have a numeric class.
            switch (type) {
                case MLArray.mxDOUBLE_CLASS:
                    mlArray = new MLDouble(name, dims, type, attributes);
                    break;
                case MLArray.mxSINGLE_CLASS:
                    mlArray = new MLSingle(name, dims, type, attributes);
                    break;
                case MLArray.mxUINT8_CLASS:
                    mlArray = new MLUInt8(name, dims, type, attributes);
                    break;
                case MLArray.mxINT8_CLASS:
                    mlArray = new MLInt8(name, dims, type, attributes);
                    break;
                case MLArray.mxUINT16_CLASS:
                    mlArray = new MLUInt16(name, dims, type, attributes);
                    break;
                case MLArray.mxINT16_CLASS:
                    mlArray = new MLInt16(name, dims, type, attributes);
                    break;
                case MLArray.mxUINT32_CLASS:
                    mlArray = new MLUInt32(name, dims, type, attributes);
                    break;
                case MLArray.mxINT32_CLASS:
                    mlArray = new MLInt32(name, dims, type, attributes);
                    break;
                case MLArray.mxINT64_CLASS:
                    mlArray = new MLInt64(name, dims, type, attributes);
                    break;
                case MLArray.mxUINT64_CLASS:
                    mlArray = new MLUInt64(name, dims, type, attributes);
                    break;
                default:
                    throw new MatlabIOException("Unsupported matlab array class: " + MLArray.typeToString(type));
            }

            // Read real.
            tag = new ISMatTag(buf);
            tag.readToByteBuffer( ((MLNumericArray<?>) mlArray).getRealByteBuffer(),
                                        (MLNumericArray<?>) mlArray );
            // Read complex.
            if (mlArray.isComplex()) {
                tag = new ISMatTag(buf);
                tag.readToByteBuffer( ((MLNumericArray<?>) mlArray).getImaginaryByteBuffer(),
                        (MLNumericArray<?>) mlArray );
            } else if (mlArray.isLogical()) {
                // If it's not complex and is logical, convert it to a logical
                // array.
                mlArray = new MLLogical((MLNumericArray<?>)mlArray);
            }
        }

        return mlArray;
    }

    byte[] bytes;
    /**
     * Converts byte array to <code>String</code>.
     *
     * It assumes that String ends with \0 value.
     *
     * @param bytes byte array containing the string.
     * @return String retrieved from byte array.
     * @throws IOException if reading error occurred.
     */
    private String zeroEndByteArrayToString(byte[] bytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        for (int i = 0; i < bytes.length && bytes[i] != 0; ++i)
            dos.writeByte(bytes[i]);
        return baos.toString();
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

        int[] flags = tag.readToIntArray();

        return flags;
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
    private int[] readDimension(ByteBuffer buf) throws IOException {
        ISMatTag tag = new ISMatTag(buf);
        int[] dims = tag.readToIntArray();
        return dims;
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
    private String readName(ByteBuffer buf) throws IOException {
        String s;

        ISMatTag tag = new ISMatTag(buf);
        char[] ac = tag.readToCharArray();
        s = new String(ac);

        return s;
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
    private void readHeader(ByteBuffer buf) throws IOException {
        // Header values.
        String description;
        int version;
        byte[] endianIndicator = new byte[2];

        // Descriptive text 116 bytes.
        byte[] descriptionBuffer = new byte[116];
        buf.get(descriptionBuffer);

        description = zeroEndByteArrayToString(descriptionBuffer);

        if (!description.matches("MATLAB 5.0 MAT-file.*"))
            throw new MatlabIOException("This is not a valid MATLAB 5.0 MAT-file.");

        // Subsyst data offset 8 bytes.
        buf.position(buf.position() + 8);

        byte[] bversion = new byte[2];
        // Version 2 bytes.
        buf.get(bversion);

        // Endian indicator 2 bytes.
        buf.get(endianIndicator);

        // Program reading the MAT-file must perform byte swapping to interpret
        // the data in the MAT-file correctly.
        if ((char)endianIndicator[0] == 'I' && (char)endianIndicator[1] == 'M') {
            byteOrder = ByteOrder.LITTLE_ENDIAN;
            version = bversion[1] & 0xff | bversion[0] << 8;
        } else {
            byteOrder = ByteOrder.BIG_ENDIAN;
            version = bversion[0] & 0xff | bversion[1] << 8;
        }

        buf.order(byteOrder);

        matFileHeader = new MatFileHeader(description, version, endianIndicator);
    }
    /**
     * TAG operator. Facilitates reading operations.
     *
     * <i>Note: reading from buffer modifies it's position</i>
     *
     * @author Wojciech Gradkowski (<a href="mailto:wgradkowski@gmail.com">wgradkowski@gmail.com</a>)
     */
    private static class ISMatTag extends MatTag {
        public ByteBuffer buf;
        private int padding;
        private boolean compressed;

        public ISMatTag(ByteBuffer buf) throws IOException {
            // Must call parent constructor.
            super(0, 0);
            this.buf = buf;
            int tmp = buf.getInt();

            if (tmp >> 16 == 0) {
                // Data not packed in the tag.
                this.type = tmp;
                this.size = buf.getInt();
                this.compressed = false;
            } else {
                // Data _packed_ in the tag (compressed).
                this.size = tmp >> 16; // 2 more significant bytes
                this.type = tmp & 0xffff; // 2 less significant bytes;
                this.compressed = true;
            }
            this.padding = this.getPadding(size, compressed);
        }

        public void readToByteBuffer(ByteBuffer buff, ByteStorageSupport<?> storage) throws IOException {
            MatFileInputStream mfis = new MatFileInputStream(this.buf, this.type);
            int elements = this.getNElements();
            mfis.readToByteBuffer(buff, elements, storage);
            this.skipPadding();
        }

        public byte[] readToByteArray() throws IOException {
            // Allocate memory for array elements.
            int elements = this.getNElements();
            byte[] ab = new byte[elements];

            MatFileInputStream mfis = new MatFileInputStream(buf, this.type);

            for ( int i = 0; i < elements; i++ )
                ab[i] = mfis.readByte();

            this.skipPadding();
            return ab;
        }

        public double[] readToDoubleArray() throws IOException {
            // Allocate memory for array elements.
            int elements = this.getNElements();
            double[] ad = new double[elements];

            MatFileInputStream mfis = new MatFileInputStream(buf, type);

            for (int i = 0; i < elements; ++i)
                ad[i] = mfis.readDouble();

            this.skipPadding();
            return ad;
        }

        public int[] readToIntArray() throws IOException {
            // Allocate memory for array elements.
            int elements = this.getNElements();
            int[] ai = new int[elements];

            MatFileInputStream mfis = new MatFileInputStream(buf, type);

            for (int i = 0; i < elements; ++i)
                ai[i] = mfis.readInt();

            this.skipPadding();
            return ai;
        }

        public char[] readToCharArray() throws IOException {
            // Allocate memory for array elements.
            int elements = this.getNElements();
            char[] ac = new char[elements];

            MatFileInputStream mfis = new MatFileInputStream(buf, type);

            for (int i = 0; i < elements; ++i)
                ac[i] = mfis.readChar();

            this.skipPadding();
            return ac;
        }

        /**
         * Skip padding.
         */
        public void skipPadding() {
            if (padding > 0)
                this.buf.position(this.buf.position() + this.padding);
        }
    }
}
