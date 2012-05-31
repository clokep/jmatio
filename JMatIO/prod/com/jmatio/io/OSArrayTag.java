package com.jmatio.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Output stream array tag, a tiny class that represents MAT-file TAG.
 * It simplifies writing data. Automates writing padding for instance.
 */
public class OSArrayTag extends MatTag {
    private ByteBuffer data;
    private int padding;

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
        super(type, data.limit());
        this.data = data;
        data.rewind();
        this.padding = this.getPadding(data.limit(), false);
    }

    /**
     * Writes tag and data to <code>DataOutputStream</code>. Writes padding if
     * neccesary.
     * @param os
     * @throws IOException
     */
    public void writeTo(DataOutputStream os) throws IOException {
        // TODO Attempt to compress tags when appropriate.
        // Write the type and size.
        os.writeInt(this.type);
        os.writeInt(this.size);

        // Write all the data (or up to the maximum size) to the output stream.
        os.write(this.data.array(), 0, this.data.remaining());

        if (this.padding > 0)
            os.write(new byte[this.padding]);
    }
}
