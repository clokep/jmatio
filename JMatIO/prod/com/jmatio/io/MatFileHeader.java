package com.jmatio.io;

import java.util.Date;

/**
 * Create a MAT-file header.
 *
 * Level 5 MAT-files begin with a 128-byte header made up of a 116 byte text
 * field, 8 unused bytes and two, 16-bit flag fields.
 *
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MatFileHeader {
    private static String DEFAULT_DESCRIPTIVE_TEXT = "MATLAB 5.0 MAT-file, Platform: "
                                                   + System.getProperty("os.name")
                                                   + ", CREATED on: ";
    private static int DEFAULT_VERSION = 0x0100;
    /**
     * {@link ByteBuffer}s are always BIG_ENDIAN by default in Java. By default,
     * we don't require byte swapping.
     */
    private static byte[] DEFAULT_ENDIAN_INDICATOR = new byte[] {(byte)'M', (byte)'I'};

    private int version;
    private String description;
    private byte[] endianIndicator;

    /**
     * Creates a new <code>MatFileHeader</code> instance with default header
     * values:
     * <ul>
     *   <li>MAT-file is 5.0 version</li>
     *   <li>Version is set to 0x0100</li>
     *   <li>No byte-swapping ("MI")</li>
     * </ul>
     *
     * @return - new <code>MatFileHeader</code> instance
     */
    public MatFileHeader() {
        this(MatFileHeader.DEFAULT_DESCRIPTIVE_TEXT + (new Date()).toString(),
             MatFileHeader.DEFAULT_VERSION,
             MatFileHeader.DEFAULT_ENDIAN_INDICATOR);
    }

    /**
     * New MAT-file header
     *
     * @param description descriptive text (no longer than 116 characters)
     * @param version by default is set to 0x0100
     * @param endianIndicator byte array size of 2 indicating byte-swapping requirement
     */
    public MatFileHeader(String description, int version, byte[] endianIndicator) {
        this.description = description;
        this.version = version;
        this.endianIndicator = endianIndicator;
    }

    /**
     * Gets descriptive text
     *
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets endian indicator. Bytes written as "MI" suggest that byte-swapping operation is required
     * in order to interpret data correctly. If value is set to "IM" byte-swapping is not needed.
     *
     * @return a byte array size of 2
     */
    public byte[] getEndianIndicator() {
        return this.endianIndicator;
    }

    /**
     * When creating a MAT-file, set version to 0x0100
     *
     * @return the version of the MAT-file
     */
    public int getVersion() {
        return this.version;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append("desriptive text: " + description);
        sb.append(", version: " + version);
        sb.append(", endianIndicator: " + new String(endianIndicator) );
        sb.append("]");

        return sb.toString();
    }
}
