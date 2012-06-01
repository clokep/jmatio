package com.jmatio.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Output stream array tag, a tiny class that represents MAT-file TAG.
 * It simplifies writing data. Automates writing padding for instance.
 */
public class OSArrayTag extends MatTag {
    /**
     * Creates TAG and sets its <code>size</code> as size of byte array
     *
     * @param type
     * @param data
     */
    public OSArrayTag(int type, byte[] data) {
        this(type, ByteBuffer.wrap(data));
    }

    /**
     * Creates TAG and sets its <code>size</code> as size of byte array
     *
     * @param type
     * @param data
     */
    public OSArrayTag(int type, ByteBuffer data) {
        super(data);

        this.type = type;
        this.size = data.limit();
        // If the number of bytes is less than 4, we can fit this into a
        // compressed tag.
        this.compressed = 1 <= this.size && this.size <= 4;

        this.calculatePadding();

        data.rewind();
    }

    /**
     * Writes tag and data to <code>DataOutputStream</code>. Writes padding if
     * neccesary.
     * @param os
     * @throws IOException
     */
    public void writeTo(DataOutputStream os) throws IOException {
        // Small data element format. If the element takes up only 1 - 4 bytes,
        // the data is stored in a special 8-byte format, the number of bytes
        // stored as a 2-byte value, the data type is stored as a 2-byte value
        // and the data is stored as the final four-bytes.
        if (this.compressed) {
            //os.writeShort(this.size);
            //os.writeShort(this.type);
            os.writeInt(this.size << 16 | this.type);
        } else {
            // Write the type and size.
            os.writeInt(this.type);
            os.writeInt(this.size);
        }
        // Write all the data (or up to the maximum size) to the output stream.
        os.write(this.data.array(), 0, this.data.remaining());

        if (this.padding > 0)
            os.write(new byte[this.padding]);
    }
}
