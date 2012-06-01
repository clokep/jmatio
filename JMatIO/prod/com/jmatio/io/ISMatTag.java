package com.jmatio.io;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.jmatio.types.ByteStorageSupport;

/**
 * Input stream tag operator. Facilitates reading operations.
 *
 * <i>Note: reading from buffer modifies it's position</i>
 *
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class ISMatTag extends MatTag {
    public ISMatTag(ByteBuffer data) throws IOException {
        super(data);

        int tmp = data.getInt();
        this.compressed = tmp >> 16 != 0;

        if (!this.compressed) {
            // Data not packed in the tag.
            this.type = tmp;
            this.size = data.getInt();
        } else {
            // Data _packed_ in the tag (compressed).
            this.size = tmp >> 16; // 2 most significant bytes.
            this.type = tmp & 0xffff; // 2 less significant bytes.
        }

        this.calculatePadding();
    }

    public void readToByteBuffer(ByteBuffer buf, ByteStorageSupport<?> storage) throws IOException {
        MatFileInputStream mfis = new MatFileInputStream(this.data, this.type);
        int elements = this.getNElements();
        mfis.readToByteBuffer(buf, elements, storage);
        this.skipPadding();
    }

    public byte[] readToByteArray() throws IOException {
        // Allocate memory for array elements.
        int elements = this.getNElements();
        byte[] ab = new byte[elements];

        MatFileInputStream mfis = new MatFileInputStream(this.data, this.type);

        for ( int i = 0; i < elements; i++ )
            ab[i] = mfis.readByte();

        this.skipPadding();
        return ab;
    }

    public double[] readToDoubleArray() throws IOException {
        // Allocate memory for array elements.
        int elements = this.getNElements();
        double[] ad = new double[elements];

        MatFileInputStream mfis = new MatFileInputStream(this.data, this.type);

        for (int i = 0; i < elements; ++i)
            ad[i] = mfis.readDouble();

        this.skipPadding();
        return ad;
    }

    public int[] readToIntArray() throws IOException {
        // Allocate memory for array elements.
        int elements = this.getNElements();
        int[] ai = new int[elements];

        MatFileInputStream mfis = new MatFileInputStream(this.data, this.type);

        for (int i = 0; i < elements; ++i)
            ai[i] = mfis.readInt();

        this.skipPadding();
        return ai;
    }

    public char[] readToCharArray() throws IOException {
        // Allocate memory for array elements.
        int elements = this.getNElements();
        char[] ac = new char[elements];

        MatFileInputStream mfis = new MatFileInputStream(this.data, this.type);

        for (int i = 0; i < elements; ++i)
            ac[i] = mfis.readChar();

        this.skipPadding();
        return ac;
    }

    public String readToString() throws IOException {
        return new String(this.readToCharArray());
    }

    /**
     * Skip padding.
     */
    public void skipPadding() {
        if (padding > 0)
            this.data.position(this.data.position() + this.padding);
    }
}
