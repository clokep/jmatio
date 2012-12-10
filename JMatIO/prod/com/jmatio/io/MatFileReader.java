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
 * Reads Level MAT-files into <code>MLArray</code> objects.
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
public abstract class MatFileReader {
    public static final int MEMORY_MAPPED_FILE = 1;
    public static final int DIRECT_BYTE_BUFFER = 2;
    public static final int HEAP_BYTE_BUFFER   = 4;

    /**
     * Container for read <code>MLArray</code>s.
     */
    protected Map<String, MLArray> data;
    /**
     * Tells how bytes are organized in the buffer.
     */
    protected ByteOrder byteOrder;
    /**
     * Array name filter.
     */
    protected MatFileFilter filter;

    /**
     * Creates an instance of <code>MatFileReader</code> and reads MAT-file
     * from the location given as <code>fileName</code>.
     *
     * This method reads a MAT-file without filtering.
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
            switch (policy) {
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
                this.readData(buf);

            return this.getContent();
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
                            break; // A hell cannot be unmapped - hopefully GC
                                   // will do it's job later.
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
     * outside world, maybe it's safe to use it without being cursed by the SUN.
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
    abstract protected void readData(ByteBuffer buf) throws IOException;

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
    abstract protected MLArray readMatrix(ByteBuffer buf, boolean isRoot) throws IOException;

    /**
     * Converts byte array to <code>String</code>.
     *
     * It assumes that String ends with \0 value.
     *
     * @param bytes byte array containing the string.
     * @return String retrieved from byte array.
     * @throws IOException if reading error occurred.
     */
    protected String zeroEndByteArrayToString(byte[] bytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        for (int i = 0; i < bytes.length && bytes[i] != 0; ++i)
            dos.writeByte(bytes[i]);
        return baos.toString();
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
    abstract protected int[] readDimension(ByteBuffer buf) throws IOException;

    /**
     * Reads Matrix name.
     *
     * Modifies <code>buf</code> position.
     *
     * @param buf <code>ByteBuffer</code>
     * @return name <code>String</code>
     * @throws IOException if reading from buffer fails
     */
    abstract protected String readName(ByteBuffer buf) throws IOException;

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
    abstract protected void readHeader(ByteBuffer buf) throws IOException;
}
